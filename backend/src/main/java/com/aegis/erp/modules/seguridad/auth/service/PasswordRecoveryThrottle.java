package com.aegis.erp.modules.seguridad.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class PasswordRecoveryThrottle {
    private final ConcurrentHashMap<String, Instant> requests = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration cooldown;

    public PasswordRecoveryThrottle(
            Clock clock,
            @Value("${password-recovery.request-cooldown-seconds:60}") long cooldownSeconds) {
        this.clock = clock;
        this.cooldown = Duration.ofSeconds(Math.max(1, cooldownSeconds));
    }

    public boolean allow(String identifier) {
        Instant now = clock.instant();
        String key = digest(identifier.trim().toLowerCase(java.util.Locale.ROOT));
        AtomicBoolean allowed = new AtomicBoolean(false);
        requests.compute(
                key,
                (ignored, previous) -> {
                    if (previous == null || !now.isBefore(previous.plus(cooldown))) {
                        allowed.set(true);
                        return now;
                    }
                    return previous;
                });
        if (requests.size() > 10_000) {
            requests.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().plus(cooldown)));
        }
        return allowed.get();
    }

    private String digest(String value) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible.", exception);
        }
    }
}
