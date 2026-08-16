package org.lele.lollcuquery.lcu;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record LcuCredentials(int port, String token, String protocol) {

    public String baseUrl() {
        String scheme = protocol == null || protocol.isBlank() ? "https" : protocol;
        return scheme + "://127.0.0.1:" + port;
    }

    public String basicAuthHeader() {
        String raw = "riot:" + token;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
