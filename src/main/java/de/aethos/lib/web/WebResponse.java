package de.aethos.lib.web;

import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class WebResponse {
    private static final byte[] EMPTY = new byte[0];

    private final int status;

    @Nullable
    private final String contentType;

    private final byte[] body;

    private final List<Map.Entry<String, String>> headers;

    private WebResponse(final int status, @Nullable final String contentType, final byte[] body,
                        final List<Map.Entry<String, String>> headers) {
        this.status = status;
        this.contentType = contentType;
        this.body = body;
        this.headers = headers;
    }

    public static WebResponse of(final int status, final String contentType, final byte[] body) {
        return new WebResponse(status, contentType, body.clone(), List.of());
    }

    public static WebResponse json(final int status, final String json) {
        return text(status, "application/json; charset=utf-8", json).header("Cache-Control", "no-store");
    }

    public static WebResponse html(final int status, final String html) {
        return text(status, "text/html; charset=utf-8", html).header("Cache-Control", "no-store");
    }

    public static WebResponse text(final int status, final String text) {
        return text(status, "text/plain; charset=utf-8", text);
    }

    public static WebResponse redirect(final String location) {
        return new WebResponse(303, null, EMPTY, List.of()).header("Location", location);
    }

    public static WebResponse status(final int status) {
        return new WebResponse(status, null, EMPTY, List.of());
    }

    public WebResponse header(final String name, final String value) {
        final List<Map.Entry<String, String>> extended = new ArrayList<>(headers);
        extended.add(Map.entry(name, value));
        return new WebResponse(status, contentType, body, List.copyOf(extended));
    }

    public WebResponse cookie(final String cookie) {
        return header("Set-Cookie", cookie);
    }

    public int status() {
        return status;
    }

    @Nullable
    public String contentType() {
        return contentType;
    }

    public byte[] body() {
        return body.clone();
    }

    public List<Map.Entry<String, String>> headers() {
        return headers;
    }

    private static WebResponse text(final int status, final String contentType, final String text) {
        return new WebResponse(status, contentType, text.getBytes(StandardCharsets.UTF_8), List.of());
    }
}
