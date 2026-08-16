package org.lele.lollcuquery.lcu;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class LcuConnection {

    private final AtomicReference<LcuCredentials> credentials = new AtomicReference<>();

    public Optional<LcuCredentials> current() {
        return Optional.ofNullable(credentials.get());
    }

    public LcuCredentials require() {
        LcuCredentials value = credentials.get();
        if (value == null) {
            throw new LcuNotConnectedException();
        }
        return value;
    }

    public void update(LcuCredentials next) {
        credentials.set(next);
    }

    public void clear() {
        credentials.set(null);
    }

    public boolean isConnected() {
        return credentials.get() != null;
    }
}
