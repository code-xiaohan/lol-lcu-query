package org.lele.lollcuquery.lcu;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LcuCommandLineParser {

    private static final Pattern PORT = Pattern.compile("--app-port=(\\d+)");
    private static final Pattern TOKEN = Pattern.compile("--remoting-auth-token=([^\\s\"]+)");
    private static final Pattern INSTALL_DIR = Pattern.compile("--install-directory=(\"[^\"]+\"|\\S+)");

    private LcuCommandLineParser() {
    }

    public static Optional<LcuCredentials> parse(String commandLine) {
        if (commandLine == null || commandLine.isBlank()) {
            return Optional.empty();
        }
        Matcher portMatcher = PORT.matcher(commandLine);
        Matcher tokenMatcher = TOKEN.matcher(commandLine);
        if (!portMatcher.find() || !tokenMatcher.find()) {
            return Optional.empty();
        }
        int port = Integer.parseInt(portMatcher.group(1));
        String token = tokenMatcher.group(1);
        return Optional.of(new LcuCredentials(port, token, "https"));
    }

    public static Optional<Path> parseInstallDirectory(String commandLine) {
        if (commandLine == null || commandLine.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = INSTALL_DIR.matcher(commandLine);
        if (!matcher.find()) {
            return Optional.empty();
        }
        String dir = matcher.group(1);
        if (dir.startsWith("\"") && dir.endsWith("\"") && dir.length() >= 2) {
            dir = dir.substring(1, dir.length() - 1);
        }
        Path path = Path.of(dir);
        return Files.isDirectory(path) ? Optional.of(path) : Optional.empty();
    }

    public static Optional<LcuCredentials> parseLockfile(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }
        String[] parts = content.trim().split(":");
        if (parts.length < 5) {
            return Optional.empty();
        }
        try {
            int port = Integer.parseInt(parts[2]);
            return Optional.of(new LcuCredentials(port, parts[3], parts[4]));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}
