package de.aethos.lib.web;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class WebRequest {
    private final HttpExchange exchange;

    @Nullable
    private String body;

    WebRequest(final HttpExchange exchange) {
        this.exchange = exchange;
    }

    public String method() {
        return exchange.getRequestMethod();
    }

    public String path() {
        return exchange.getRequestURI().getPath();
    }

    public InetSocketAddress remoteAddress() {
        return exchange.getRemoteAddress();
    }

    @Nullable
    public String header(final String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    @Nullable
    public String query(final String name) {
        return param(exchange.getRequestURI().getRawQuery(), name);
    }

    @Nullable
    public String cookie(final String name) {
        final String header = header("Cookie");
        if (header == null) {
            return null;
        }
        for (final String part : header.split(";")) {
            final String[] pair = part.trim().split("=", 2);
            if (pair.length == 2 && pair[0].equals(name)) {
                return pair[1];
            }
        }
        return null;
    }

    public String body(final int maxBytes) throws IOException {
        if (body == null) {
            final byte[] data = exchange.getRequestBody().readNBytes(maxBytes + 1);
            if (data.length > maxBytes) {
                throw new IOException("Request body larger than " + maxBytes + " bytes");
            }
            body = new String(data, StandardCharsets.UTF_8);
        }
        return body;
    }

    @Nullable
    public String form(final String name, final int maxBytes) throws IOException {
        return param(body(maxBytes), name);
    }

    @Nullable
    private static String param(@Nullable final String encoded, final String name) {
        if (encoded == null) {
            return null;
        }
        for (final String pair : encoded.split("&")) {
            final int split = pair.indexOf('=');
            final String key = split < 0 ? pair : pair.substring(0, split);
            if (key.equals(name)) {
                try {
                    return split < 0 ? "" : URLDecoder.decode(pair.substring(split + 1), StandardCharsets.UTF_8);
                } catch (final IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
