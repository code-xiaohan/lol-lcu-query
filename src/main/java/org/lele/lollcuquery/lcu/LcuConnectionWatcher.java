package org.lele.lollcuquery.lcu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LcuConnectionWatcher {

    private static final Logger log = LoggerFactory.getLogger(LcuConnectionWatcher.class);

    private final LcuProcessDiscovery discovery;
    private final LcuConnection connection;
    private final LcuHttpClient httpClient;

    public LcuConnectionWatcher(LcuProcessDiscovery discovery, LcuConnection connection, LcuHttpClient httpClient) {
        this.discovery = discovery;
        this.connection = connection;
        this.httpClient = httpClient;
    }

    @Scheduled(fixedDelay = 2000)
    public void refresh() {
        Optional<LcuCredentials> discovered = discovery.discover();
        if (discovered.isEmpty()) {
            if (connection.isConnected()) {
                log.info("League client disconnected");
                connection.clear();
            }
            return;
        }
        LcuCredentials credentials = discovered.get();
        connection.update(credentials);
        try {
            httpClient.get("/lol-summoner/v1/current-summoner");
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            if (ex.getStatusCode().value() != 404) {
                log.debug("LCU handshake failed, clearing connection", ex);
                connection.clear();
            }
        } catch (Exception ex) {
            log.debug("LCU handshake failed, clearing connection", ex);
            connection.clear();
        }
    }
}
