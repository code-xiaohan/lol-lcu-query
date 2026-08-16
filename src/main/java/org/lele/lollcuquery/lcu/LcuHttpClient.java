package org.lele.lollcuquery.lcu;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class LcuHttpClient {

    private final LcuConnection connection;
    private final RestClient restClient;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public LcuHttpClient(LcuConnection connection) {
        this.connection = connection;
        this.restClient = RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(createTrustAllHttpClient()))
                .build();
    }

    public JsonNode get(String path) {
        LcuCredentials credentials = connection.require();
        return getAbsolute(credentials.baseUrl() + path, credentials.basicAuthHeader());
    }

    public Optional<JsonNode> getOptional(String path) {
        try {
            return Optional.of(get(path));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public JsonNode getLiveClient(String path) {
        return getAbsolute("https://127.0.0.1:2999" + path, null);
    }

    public Optional<JsonNode> getLiveClientOptional(String path) {
        try {
            return Optional.of(getLiveClient(path));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private JsonNode getAbsolute(String url, String authorization) {
        RestClient.RequestHeadersSpec<?> spec = restClient.get().uri(url);
        if (authorization != null) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        String body = spec
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .body(String.class);
        try {
            if (body == null || body.isBlank()) {
                return jsonMapper.nullNode();
            }
            return jsonMapper.readTree(body.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse LCU JSON from " + url, ex);
        }
    }

    private static CloseableHttpClient createTrustAllHttpClient() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                    .build();
            DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
            return HttpClients.custom()
                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                            .setTlsSocketStrategy(tlsStrategy)
                            .build())
                    .evictExpiredConnections()
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create LCU HTTP client", ex);
        }
    }
}
