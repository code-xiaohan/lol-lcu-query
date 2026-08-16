package org.lele.lollcuquery.lcu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class LcuProcessDiscovery {

    private static final Logger log = LoggerFactory.getLogger(LcuProcessDiscovery.class);

    public Optional<LcuCredentials> discover() {
        List<String> commandLines = readLeagueClientCommandLines();
        for (String commandLine : commandLines) {
            Optional<LcuCredentials> fromArgs = LcuCommandLineParser.parse(commandLine);
            if (fromArgs.isPresent()) {
                return fromArgs;
            }
            Optional<Path> installDir = LcuCommandLineParser.parseInstallDirectory(commandLine);
            if (installDir.isPresent()) {
                Optional<LcuCredentials> fromLockfile = readLockfile(installDir.get().resolve("lockfile"));
                if (fromLockfile.isPresent()) {
                    return fromLockfile;
                }
            }
        }
        for (Path candidate : commonLockfilePaths()) {
            Optional<LcuCredentials> fromLockfile = readLockfile(candidate);
            if (fromLockfile.isPresent()) {
                return fromLockfile;
            }
        }
        return Optional.empty();
    }

    private List<String> readLeagueClientCommandLines() {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        try {
            ProcessBuilder builder = windows
                    ? new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-NonInteractive",
                    "-Command",
                    "Get-CimInstance Win32_Process -Filter \"Name='LeagueClientUx.exe'\" | Select-Object -ExpandProperty CommandLine")
                    : new ProcessBuilder("ps", "-ax", "-o", "command=");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return List.of();
            }
            Charset charset = windows ? Charset.forName("GBK") : StandardCharsets.UTF_8;
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("LeagueClientUx") && line.contains("remoting-auth-token")) {
                        lines.add(line);
                    }
                }
            }
            return lines;
        } catch (Exception ex) {
            log.debug("Failed to inspect LeagueClient process", ex);
            return List.of();
        }
    }

    private Optional<LcuCredentials> readLockfile(Path lockfile) {
        try {
            if (!Files.isRegularFile(lockfile)) {
                return Optional.empty();
            }
            String content = Files.readString(lockfile, StandardCharsets.UTF_8);
            return LcuCommandLineParser.parseLockfile(content);
        } catch (Exception ex) {
            log.debug("Failed to read lockfile {}", lockfile, ex);
            return Optional.empty();
        }
    }

    private List<Path> commonLockfilePaths() {
        List<Path> paths = new ArrayList<>();
        String userHome = System.getProperty("user.home", "");
        paths.add(Path.of("C:\\Riot Games\\League of Legends\\lockfile"));
        paths.add(Path.of("D:\\Riot Games\\League of Legends\\lockfile"));
        paths.add(Path.of("C:\\Program Files\\Riot Games\\League of Legends\\lockfile"));
        paths.add(Path.of("/Applications/League of Legends.app/Contents/LoL/lockfile"));
        if (!userHome.isBlank()) {
            paths.add(Path.of(userHome, "Applications", "League of Legends.app", "Contents", "LoL", "lockfile"));
        }
        return paths;
    }
}
