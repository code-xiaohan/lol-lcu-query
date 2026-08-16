package org.lele.lollcuquery.lcu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LcuCommandLineParserTest {

    @Test
    void parsesPortAndTokenFromCommandLine() {
        String commandLine = "\"C:\\\\Riot Games\\\\League of Legends\\\\LeagueClientUx.exe\" "
                + "--app-port=54321 --remoting-auth-token=AbCdEf123 --install-directory=\"C:\\\\Riot Games\\\\League of Legends\"";
        var credentials = LcuCommandLineParser.parse(commandLine);
        assertTrue(credentials.isPresent());
        assertEquals(54321, credentials.get().port());
        assertEquals("AbCdEf123", credentials.get().token());
        assertTrue(credentials.get().basicAuthHeader().startsWith("Basic "));
    }

    @Test
    void parsesLockfile() {
        var credentials = LcuCommandLineParser.parseLockfile("LeagueClient:1234:50931:tokenValue:https");
        assertTrue(credentials.isPresent());
        assertEquals(50931, credentials.get().port());
        assertEquals("tokenValue", credentials.get().token());
        assertEquals("https://127.0.0.1:50931", credentials.get().baseUrl());
    }
}
