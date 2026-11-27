package de.aethos.lib.resourcepack.pack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.aethos.lib.resourcepack.ResourcePackManager;
import de.aethos.lib.web.PluginWebServer;
import de.aethos.lib.web.SessionAuth;
import de.aethos.lib.web.WebRequest;
import de.aethos.lib.web.WebResponse;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Web {

    private final Plugin plugin;

    private final ResourcePackManager manager;

    private final SessionAuth auth;

    private final String index;

    private final String login;

    private final int uploadLimit;

    private Web(final Plugin plugin, final ResourcePackManager manager, final SessionAuth auth, final String index,
                    final String login, final int uploadLimit) {
        this.plugin = plugin;
        this.manager = manager;
        this.auth = auth;
        this.index = index;
        this.login = login;
        this.uploadLimit = uploadLimit;
    }

    public static PluginWebServer create(final Plugin plugin, final ResourcePackManager manager,
                                         final ResourcePackManager.Settings settings, final SessionAuth auth) throws IOException {
        final Web pages = new Web(plugin, manager, auth, resource(plugin, "web/resourcepack.html"),
                resource(plugin, "web/resourcepack-login.html"), settings.uploadLimit());
        return PluginWebServer.create("AethosRP", plugin.getLogger(), settings.bind(), settings.port(), 3)
                .route("/", pages::root)
                .route("/login", pages::login)
                .route("/pack/", pages::pack)
                .route("/status", auth, pages::status)
                .route("/upload", auth, pages::upload)
                .route("/build", auth, pages::build)
                .route("/config", auth, pages::config)
                .route("/layer", auth, pages::layer);
    }

    private static String resource(final Plugin plugin, final String path) throws IOException {
        try (InputStream in = plugin.getResource(path)) {
            if (in == null) {
                throw new IOException(path + " fehlt");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static WebResponse result(final int status, final boolean ok) {
        final JsonObject json = new JsonObject();
        json.addProperty("ok", ok);
        return WebResponse.json(status, json.toString());
    }

    private static WebResponse error(final int status, final String message) {
        final JsonObject json = new JsonObject();
        json.addProperty("ok", false);
        json.addProperty("error", message);
        return WebResponse.json(status, json.toString());
    }

    private static JsonArray array(final List<String> values) {
        final JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private String loginPage(final String error) {
        return login.replace("{error}", error);
    }

    private WebResponse login(final WebRequest request) throws IOException {
        final boolean form = "POST".equals(request.method());
        final String token = request.query("token");
        if (!form && token == null) {
            return WebResponse.redirect("/");
        }
        final String cookie = form ? auth.login(request.form("password", 4096)) : auth.redeem(token);
        if (cookie == null) {
            plugin.getLogger().warning("Web-Login fehlgeschlagen von " + request.remoteAddress());
            return WebResponse.html(401, loginPage(form ? "Falsches Passwort." : "Link ungültig oder abgelaufen."));
        }
        return WebResponse.redirect("/").cookie(cookie);
    }

    private WebResponse root(final WebRequest request) {
        if (!"/".equals(request.path())) {
            return WebResponse.text(404, "Not found");
        }
        return WebResponse.html(200, auth.check(request) ? index : loginPage(""));
    }

    private WebResponse pack(final WebRequest request) {
        final String name = request.path().substring("/pack/".length());
        if (!name.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            return WebResponse.text(404, "Not found");
        }
        final String id = name.substring(0, name.length() - 4);
        final Path file = manager.packFile(id);
        if (file == null) {
            return WebResponse.text(404, "Pack nicht gefunden.");
        }
        final String forwarded = request.header("X-Forwarded-For");
        plugin.getLogger().info("[Pack] Download " + id + ".zip von " + request.remoteAddress().getAddress().getHostAddress()
                + (forwarded == null ? "" : " (X-Forwarded-For: " + forwarded + ")"));
        return WebResponse.file(file, "application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + id + ".zip\"");
    }

    private WebResponse status(final WebRequest request) {
        final ResourcePackManager.Settings settings = manager.settings();
        final List<Artifact> artifacts = manager.artifacts();
        final JsonObject json = new JsonObject();
        json.addProperty("built", !artifacts.isEmpty());
        json.addProperty("building", manager.isBuilding());
        json.addProperty("baseUrl", manager.baseUrl());
        json.addProperty("publicUrlCfg", settings.publicUrl());
        json.addProperty("compression", settings.compression());
        json.addProperty("validate", settings.validate());
        json.addProperty("lastBuild", manager.lastBuildTime());

        final JsonArray packs = new JsonArray();
        final JsonArray log = new JsonArray();
        for (final Artifact artifact : artifacts) {
            final Artifact.Validation validation = artifact.validation();
            final JsonObject pack = new JsonObject();
            pack.addProperty("name", artifact.name());
            pack.addProperty("id", artifact.id());
            pack.addProperty("ok", artifact.ok());
            pack.addProperty("sendable", artifact.sendable());
            pack.addProperty("size", artifact.ok() ? artifact.sizeHuman() : "-");
            pack.addProperty("format", artifact.packFormat());
            pack.addProperty("sha1", artifact.sha1Short());
            pack.add("errors", array(!artifact.ok() ? List.of(artifact.message())
                    : validation == null ? List.of() : validation.errors()));
            pack.add("warnings", array(validation == null ? List.of() : validation.warnings()));
            packs.add(pack);
            artifact.log().forEach(log::add);
        }
        json.add("packs", packs);

        final JsonArray layers = new JsonArray();
        for (final ResourcePackManager.Layer layer : manager.layers()) {
            final JsonObject entry = new JsonObject();
            entry.addProperty("name", layer.name());
            entry.addProperty("type", layer.type());
            entry.addProperty("ref", layer.ref());
            entry.addProperty("enabled", layer.enabled());
            entry.addProperty("present", manager.layerPresent(layer));
            layers.add(entry);
        }
        json.add("layers", layers);
        json.add("uploads", array(manager.uploads()));
        json.add("log", log);
        return WebResponse.json(200, json.toString());
    }

    private WebResponse upload(final WebRequest request) throws IOException {
        final String name = request.query("name");
        if ("DELETE".equals(request.method())) {
            final boolean deleted = manager.deleteUpload(name);
            if (deleted) {
                sync(this::refreshLayers);
            }
            return result(deleted ? 200 : 404, deleted);
        }
        if (!"POST".equals(request.method())) {
            return WebResponse.text(405, "Nur POST.");
        }
        if (tooLarge(request.header("Content-Length"))) {
            return error(413, "Datei zu groß");
        }
        final byte[] body = request.bytes(uploadLimit);
        if (body.length < 4 || body[0] != 'P' || body[1] != 'K') {
            return error(400, "keine ZIP-Datei");
        }
        final String saved = manager.saveUpload(name, body);
        if (saved == null) {
            return error(400, "ungültiger Name");
        }
        sync(this::refreshLayers);
        final JsonObject json = new JsonObject();
        json.addProperty("ok", true);
        json.addProperty("name", saved);
        return WebResponse.json(200, json.toString());
    }

    private WebResponse build(final WebRequest request) throws IOException {
        if (!"POST".equals(request.method())) {
            return WebResponse.text(405, "Nur POST.");
        }
        sync(manager::rebuild);
        final JsonObject json = new JsonObject();
        json.addProperty("ok", true);
        json.addProperty("building", manager.isBuilding());
        return WebResponse.json(200, json.toString());
    }

    private WebResponse config(final WebRequest request) throws IOException {
        if (!"POST".equals(request.method())) {
            return WebResponse.text(405, "Nur POST.");
        }
        final String url = request.query("public-url");
        if (sync(() -> manager.setPublicUrl(url))) {
            return result(200, true);
        }
        return error(400, "URL muss mit http:// oder https:// beginnen");
    }

    private WebResponse layer(final WebRequest request) throws IOException {
        if (!"POST".equals(request.method())) {
            return WebResponse.text(405, "Nur POST.");
        }
        final String action = request.query("action");
        final String ref = request.query("ref");
        if (action == null || ref == null) {
            return result(400, false);
        }
        final boolean changed = sync(() -> switch (action) {
            case "up" -> manager.moveLayer(ref, -1);
            case "down" -> manager.moveLayer(ref, 1);
            case "toggle" -> manager.toggleLayer(ref);
            default -> false;
        });
        return result(changed ? 200 : 400, changed);
    }

    private boolean refreshLayers() {
        manager.syncLayers();
        return true;
    }

    private boolean tooLarge(final String contentLength) {
        if (contentLength == null) {
            return false;
        }
        try {
            return Long.parseLong(contentLength.trim()) > uploadLimit;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    private <T> T sync(final Callable<T> task) throws IOException {
        try {
            return plugin.getServer().getScheduler().callSyncMethod(plugin, task).get(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        } catch (final ExecutionException | TimeoutException | RuntimeException e) {
            throw new IOException(e);
        }
    }
}
