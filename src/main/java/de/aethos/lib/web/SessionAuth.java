package de.aethos.lib.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionAuth {
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private static final SecureRandom RANDOM = new SecureRandom();

    private final String cookieName;

    private final byte[] passwordBytes;

    private final boolean enabled;

    private final long ttlMillis;

    private final Map<String, Long> sessions = new ConcurrentHashMap<>();

    public SessionAuth(final String cookieName, final String password) {
        this(cookieName, password, DEFAULT_TTL);
    }

    public SessionAuth(final String cookieName, final String password, final Duration ttl) {
        this.cookieName = cookieName;
        this.passwordBytes = (password == null ? "" : password).getBytes(StandardCharsets.UTF_8);
        this.enabled = password != null && !password.isBlank();
        this.ttlMillis = ttl.toMillis();
    }

    public boolean enabled() {
        return enabled;
    }

    public String login(final String password) {
        final byte[] given = (password == null ? "" : password).getBytes(StandardCharsets.UTF_8);
        if (!enabled || !MessageDigest.isEqual(passwordBytes, given)) {
            return null;
        }
        final long now = System.currentTimeMillis();
        sessions.values().removeIf(expiry -> expiry < now);

        final byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        final String token = HexFormat.of().formatHex(raw);
        sessions.put(token, now + ttlMillis);
        return cookieName + "=" + token + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=" + ttlMillis / 1000;
    }

    public boolean check(final WebRequest request) {
        return !enabled || checkCookie(request.header("Cookie"));
    }

    public String logout(final WebRequest request) {
        final String token = token(request.header("Cookie"));
        if (token != null) {
            sessions.remove(token);
        }
        return cookieName + "=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0";
    }

    public int activeSessions() {
        final long now = System.currentTimeMillis();
        return (int) sessions.values().stream().filter(expiry -> expiry >= now).count();
    }

    boolean checkCookie(final String header) {
        final String token = token(header);
        if (token == null) {
            return false;
        }
        final Long expiry = sessions.get(token);
        if (expiry == null) {
            return false;
        }
        if (expiry < System.currentTimeMillis()) {
            sessions.remove(token);
            return false;
        }
        return true;
    }

    private String token(final String header) {
        if (header == null) {
            return null;
        }
        for (final String part : header.split(";")) {
            final String[] pair = part.trim().split("=", 2);
            if (pair.length == 2 && pair[0].equals(cookieName)) {
                return pair[1];
            }
        }
        return null;
    }
}
