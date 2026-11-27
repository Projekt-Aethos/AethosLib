package de.aethos.lib.web;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PluginWebServer {

    private final HttpServer server;

    private final ExecutorService executor;

    private final Logger logger;

    private final String bind;

    private PluginWebServer(final HttpServer server, final ExecutorService executor, final Logger logger, final String bind) {
        this.server = server;
        this.executor = executor;
        this.logger = logger;
        this.bind = bind;
    }

    public static PluginWebServer create(final String name, final Logger logger, final String bind,
                                         final int port, final int threads) throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(bind, port), 0);
        final AtomicInteger count = new AtomicInteger();
        final ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, threads), runnable -> {
            final Thread thread = new Thread(runnable, name + "-Web-" + count.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        server.setExecutor(executor);
        return new PluginWebServer(server, executor, logger, bind);
    }

    public static String jsonEscape(final String value) {
        if (value == null) {
            return "";
        }
        final StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    public static String htmlEscape(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    public PluginWebServer route(final String path, final WebHandler handler) {
        server.createContext(path, adapt(handler));
        return this;
    }

    public PluginWebServer route(final String path, final SessionAuth auth, final WebHandler handler) {
        return route(path, request -> {
            if (auth.check(request)) {
                return handler.handle(request);
            }
            return WebResponse.text(401, "Nicht angemeldet.");
        });
    }

    public PluginWebServer login(final String path, final SessionAuth auth, final String target,
                                 final WebHandler rejected) {
        return route(path, request -> {
            if (!"POST".equals(request.method())) {
                return WebResponse.text(405, "Nur POST.");
            }
            final String cookie = auth.login(request.form("password", 4096));
            if (cookie == null) {
                logger.warning("Web-Login fehlgeschlagen von " + request.remoteAddress());
                return rejected.handle(request);
            }
            return WebResponse.redirect(target).cookie(cookie);
        });
    }

    public void start() {
        server.start();
        logger.info("Web-Server läuft auf http://" + bind + ":" + port() + "/");
    }

    public void stop() {
        server.stop(0);
        executor.shutdownNow();
    }

    public int port() {
        return server.getAddress().getPort();
    }

    private HttpHandler adapt(final WebHandler handler) {
        return exchange -> {
            try (exchange) {
                final WebRequest request = new WebRequest(exchange);
                WebResponse response;
                try {
                    response = Objects.requireNonNull(handler.handle(request), "handler returned no response");
                } catch (final IOException e) {
                    return;
                } catch (final RuntimeException e) {
                    logger.log(Level.WARNING, "Web-Anfrage fehlgeschlagen: " + request.method() + " " + request.path(), e);
                    response = WebResponse.text(500, "Interner Fehler.");
                }
                write(exchange, response);
            } catch (final IOException ignored) {
            }
        };
    }

    private static void write(final HttpExchange exchange, final WebResponse response) throws IOException {
        for (final Map.Entry<String, String> header : response.headers()) {
            exchange.getResponseHeaders().add(header.getKey(), header.getValue());
        }
        final String contentType = response.contentType();
        if (contentType != null) {
            exchange.getResponseHeaders().set("Content-Type", contentType);
        }
        final Path file = response.file();
        if (file != null) {
            final long size = Files.size(file);
            if (size == 0 || "HEAD".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(response.status(), -1);
                return;
            }
            exchange.sendResponseHeaders(response.status(), size);
            try (OutputStream out = exchange.getResponseBody()) {
                Files.copy(file, out);
            }
            return;
        }
        final byte[] body = response.body();
        if (body.length == 0 || "HEAD".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(response.status(), -1);
            return;
        }
        exchange.sendResponseHeaders(response.status(), body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
