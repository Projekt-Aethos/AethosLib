package de.aethos.lib.resourcepack;

import de.aethos.lib.resourcepack.pack.Artifact;
import de.aethos.lib.resourcepack.pack.Assembler;
import de.aethos.lib.resourcepack.pack.BuiltEvent;
import de.aethos.lib.resourcepack.pack.Format;
import de.aethos.lib.resourcepack.pack.Provider;
import de.aethos.lib.resourcepack.pack.Sender;
import de.aethos.lib.resourcepack.pack.Web;
import de.aethos.lib.web.PluginWebServer;
import de.aethos.lib.web.SessionAuth;
import de.aethos.lib.web.WebServerManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class ResourcePackManager {

    private static final String FOLDER = "resourcepack";

    private static final String LAYERS_PATH = Settings.CONFIG_PATH + "layers";

    private static final Format FALLBACK_FORMAT = new Format(69, 0);

    private static final Duration TOKEN_TTL = Duration.ofMinutes(5);

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Plugin plugin;

    private final WebServerManager webServers;

    private final Sender sender;

    private final Map<String, Provider> providers = new ConcurrentHashMap<>();

    private final Map<String, Plugin> owners = new HashMap<>();

    private final AtomicBoolean building = new AtomicBoolean();

    private volatile Settings settings = Settings.DISABLED;

    private volatile List<Layer> layers = List.of();

    private volatile List<Artifact> artifacts = List.of();

    @Nullable
    private volatile Artifact combined;

    private volatile String lastBuildTime = "nie";

    private volatile String host = "127.0.0.1";

    private volatile Format format = FALLBACK_FORMAT;

    private Path sourcesDir;

    private Path workDir;

    private Path packsDir;

    private Path combinedPack;

    @Nullable
    private PluginWebServer web;

    @Nullable
    private SessionAuth auth;

    public ResourcePackManager(final Plugin plugin, final WebServerManager webServers) {
        this.plugin = plugin;
        this.webServers = webServers;
        this.sender = new Sender(plugin, this);
    }

    private static String uniqueId(final String name, final Set<String> used) {
        String base = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "pack";
        }
        String id = base;
        for (int i = 2; !used.add(id); i++) {
            id = base + "-" + i;
        }
        return id;
    }

    @Nullable
    private static String sanitize(@Nullable final String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String base = name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).trim().replaceAll("[^A-Za-z0-9._ -]", "_");
        if (base.isBlank() || base.contains("..")) {
            return null;
        }
        return base.toLowerCase(Locale.ROOT).endsWith(".zip") ? base : base + ".zip";
    }

    private static String withPort(final String base, final int port) {
        if (!base.startsWith("http://")) {
            return base;
        }
        final String rest = base.substring("http://".length());
        final int slash = rest.indexOf('/');
        final String authority = slash < 0 ? rest : rest.substring(0, slash);
        if (authority.contains(":")) {
            return base;
        }
        return "http://" + authority + ":" + port + (slash < 0 ? "" : rest.substring(slash));
    }

    public void enable() {
        final Path data = plugin.getDataFolder().toPath().resolve(FOLDER);
        sourcesDir = data.resolve("sources");
        workDir = data.resolve("work");
        packsDir = data.resolve("output").resolve("packs");
        combinedPack = data.resolve("output").resolve("server-pack.zip");
        plugin.getServer().getPluginManager().registerEvents(sender, plugin);
        apply();
        final int delay = settings.startDelay();
        if (settings.enabled() && delay > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                rebuild();
            }, delay * 20L);
        }
    }

    public boolean isEnabled() {
        return settings.enabled();
    }

    public void register(final Plugin owner, final String name, final Provider provider) {
        final String ref = owner.getName() + ":" + name;
        providers.put(ref, provider);
        owners.put(ref, owner);
        if (settings.enabled()) {
            syncLayers();
        }
    }

    public void unregister(final Plugin owner) {
        owners.entrySet().removeIf(entry -> {
            if (!entry.getValue().equals(owner)) {
                return false;
            }
            providers.remove(entry.getKey());
            return true;
        });
    }

    public boolean rebuild() {
        return rebuild(null);
    }

    public int send(final Player player) {
        return sender.send(player);
    }

    void reload() {
        plugin.reloadConfig();
        apply();
    }

    boolean rebuild(@Nullable final CommandSender notify) {
        final Settings current = settings;
        if (!current.enabled() || !building.compareAndSet(false, true)) {
            return false;
        }
        try {
            syncLayers();
            final List<Layer> snapshot = layers;
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    build(current, snapshot, notify);
                } catch (final RuntimeException e) {
                    plugin.getLogger().log(Level.WARNING, "Pack-Build fehlgeschlagen", e);
                } finally {
                    building.set(false);
                }
            });
            return true;
        } catch (final RuntimeException e) {
            building.set(false);
            throw e;
        }
    }

    public Settings settings() {
        return settings;
    }

    Sender sender() {
        return sender;
    }

    public List<Layer> layers() {
        return layers;
    }

    public List<Artifact> artifacts() {
        return artifacts;
    }

    @Nullable
    Artifact combined() {
        return combined;
    }

    public String lastBuildTime() {
        return lastBuildTime;
    }

    public boolean isBuilding() {
        return building.get();
    }

    public List<Artifact> sendable() {
        final boolean strict = settings.validateRequired();
        return artifacts.stream().filter(artifact -> strict ? artifact.sendable() : artifact.ok()).toList();
    }

    public String baseUrl() {
        final Settings current = settings;
        if (current.hasPublicUrl()) {
            return withPort(current.publicUrl().trim().replaceAll("/+$", ""), current.port());
        }
        final String bind = current.bind();
        return "http://" + (bind.isBlank() || bind.equals("0.0.0.0") ? host : bind) + ":" + current.port();
    }

    @Nullable
    String loginUrl() {
        return auth == null ? null : websiteUrl() + "/login?token=" + auth.issueToken(TOKEN_TTL);
    }

    String websiteUrl() {
        return settings.hasPublicUrl() ? baseUrl() : "https://aethosserver.de:" + settings.port();
    }

    public String packUrl(final Artifact artifact) {
        return baseUrl() + "/pack/" + artifact.id() + ".zip";
    }

    @Nullable
    public Path packFile(final String id) {
        if (id.isBlank() || id.contains("..") || id.contains("/") || id.contains("\\")) {
            return null;
        }
        final Path file = packsDir.resolve(id + ".zip").normalize();
        return file.startsWith(packsDir) && Files.isRegularFile(file) ? file : null;
    }

    public boolean setPublicUrl(@Nullable final String url) {
        String value = url == null ? "" : url.trim();
        if (!value.isEmpty()) {
            if (!value.startsWith("http://") && !value.startsWith("https://")) {
                return false;
            }
            value = value.replaceAll("/+$", "");
        }
        plugin.getConfig().set(Settings.CONFIG_PATH + "public-url", value);
        plugin.saveConfig();
        settings = Settings.read(plugin.getConfig());
        plugin.getLogger().info("Öffentliche Pack-URL gesetzt: " + (value.isEmpty() ? "(automatisch)" : value));
        return true;
    }

    @Nullable
    Layer findLayer(final String ref) {
        for (final Layer layer : layers) {
            if (layer.ref().equalsIgnoreCase(ref) || layer.name().equalsIgnoreCase(ref)) {
                return layer;
            }
        }
        return null;
    }

    public boolean moveLayer(final String ref, final int delta) {
        final Layer layer = findLayer(ref);
        if (layer == null) {
            return false;
        }
        final List<Layer> updated = new ArrayList<>(layers);
        final int from = updated.indexOf(layer);
        final int to = Math.clamp(from + delta, 0, updated.size() - 1);
        if (from == to) {
            return false;
        }
        updated.remove(from);
        updated.add(to, layer);
        saveLayers(updated);
        return true;
    }

    boolean setLayerEnabled(final String ref, final boolean enabled) {
        final Layer layer = findLayer(ref);
        if (layer == null) {
            return false;
        }
        if (layer.enabled() != enabled) {
            final List<Layer> updated = new ArrayList<>(layers);
            updated.set(updated.indexOf(layer), layer.withEnabled(enabled));
            saveLayers(updated);
        }
        return true;
    }

    public boolean toggleLayer(final String ref) {
        final Layer layer = findLayer(ref);
        return layer != null && setLayerEnabled(layer.ref(), !layer.enabled());
    }

    public boolean layerPresent(final Layer layer) {
        if (layer.isProvider()) {
            return providers.containsKey(layer.ref());
        }
        try {
            return Files.exists(layerPath(layer));
        } catch (final RuntimeException e) {
            return false;
        }
    }

    public List<String> uploads() {
        if (!Files.isDirectory(sourcesDir)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(sourcesDir)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".zip"))
                    .sorted().toList();
        } catch (final IOException e) {
            return List.of();
        }
    }

    @Nullable
    public String saveUpload(@Nullable final String name, final byte[] data) throws IOException {
        final String safe = sanitize(name);
        if (safe == null) {
            return null;
        }
        Files.createDirectories(sourcesDir);
        Files.write(sourcesDir.resolve(safe), data);
        plugin.getLogger().info("Pack-Upload gespeichert: " + safe + " (" + Assembler.human(data.length) + ")");
        return safe;
    }

    public boolean deleteUpload(@Nullable final String name) throws IOException {
        final String safe = sanitize(name);
        return safe != null && Files.deleteIfExists(sourcesDir.resolve(safe));
    }

    public void syncLayers() {
        final List<Layer> updated = new ArrayList<>(layers);
        updated.removeIf(layer -> layer.isUpload() && !Files.exists(sourcesDir.resolve(layer.ref())));
        for (final String file : uploads()) {
            if (updated.stream().noneMatch(layer -> layer.isUpload() && layer.ref().equals(file))) {
                final Layer layer = new Layer(file.replaceFirst("(?i)\\.zip$", ""), Layer.UPLOAD, file, true);
                if (settings.newUploadOnTop()) {
                    updated.addFirst(layer);
                } else {
                    updated.add(layer);
                }
            }
        }
        for (final String ref : new TreeSet<>(providers.keySet())) {
            if (updated.stream().noneMatch(layer -> layer.isProvider() && layer.ref().equals(ref))) {
                updated.add(new Layer(ref.substring(ref.indexOf(':') + 1), Layer.PROVIDER, ref, true));
            }
        }
        if (!updated.equals(layers)) {
            saveLayers(updated);
        }
    }

    private void apply() {
        stopWeb();
        settings = Settings.read(plugin.getConfig());
        if (!settings.enabled()) {
            return;
        }
        try {
            Files.createDirectories(sourcesDir);
            Files.createDirectories(packsDir);
        } catch (final IOException e) {
            plugin.getLogger().warning("Resourcepack-Ordner konnten nicht angelegt werden: " + e.getMessage());
        }
        final Format detected = Format.ofServer(plugin.getServer());
        if (detected == null) {
            plugin.getLogger().warning("Resource-Pack-Format unbekannt – es gilt " + FALLBACK_FORMAT.major() + ".");
        }
        format = detected == null ? FALLBACK_FORMAT : detected;
        host = localHost();
        layers = readLayers();
        syncLayers();
        startWeb();
    }

    private void startWeb() {
        final Settings current = settings;
        if (current.password().isBlank()) {
            plugin.getLogger().warning("resourcepack.web.password ist leer – die Pack-Verwaltung ist ohne Login erreichbar.");
        }
        try {
            final SessionAuth created = new SessionAuth("arp", current.password());
            web = webServers.register(plugin, Web.create(plugin, this, current, created));
            auth = created;
        } catch (final IOException | RuntimeException e) {
            plugin.getLogger().log(Level.SEVERE, "Resourcepack-Web-Server konnte nicht starten", e);
        }
    }

    private void stopWeb() {
        if (web != null) {
            webServers.unregister(plugin, web);
            web = null;
            auth = null;
        }
    }

    private String localHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (final IOException e) {
            return "127.0.0.1";
        }
    }

    private List<Layer> readLayers() {
        final List<Layer> read = new ArrayList<>();
        for (final Map<?, ?> entry : plugin.getConfig().getMapList(LAYERS_PATH)) {
            final Object ref = entry.get("ref");
            if (ref == null) {
                continue;
            }
            final Object name = entry.get("name");
            final Object type = entry.get("type");
            final Object enabled = entry.get("enabled");
            read.add(new Layer(String.valueOf(name == null ? ref : name),
                    String.valueOf(type == null ? Layer.PATH : type), String.valueOf(ref),
                    enabled == null || Boolean.parseBoolean(String.valueOf(enabled))));
        }
        return List.copyOf(read);
    }

    private void saveLayers(final List<Layer> updated) {
        layers = List.copyOf(updated);
        final List<Map<String, Object>> out = new ArrayList<>();
        for (final Layer layer : updated) {
            final Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", layer.name());
            entry.put("type", layer.type());
            entry.put("ref", layer.ref());
            entry.put("enabled", layer.enabled());
            out.add(entry);
        }
        plugin.getConfig().set(LAYERS_PATH, out);
        plugin.saveConfig();
    }

    private Path layerPath(final Layer layer) {
        if (layer.isUpload()) {
            return sourcesDir.resolve(layer.ref());
        }
        final Path path = Path.of(layer.ref());
        return path.isAbsolute() ? path : plugin.getDataFolder().toPath().resolve(path).normalize();
    }

    private Path source(final Layer layer) throws IOException {
        if (!layer.isProvider()) {
            return layerPath(layer);
        }
        final Provider provider = providers.get(layer.ref());
        if (provider == null) {
            throw new IOException("kein Provider registriert");
        }
        return provider.provide();
    }

    private void build(final Settings current, final List<Layer> snapshot, @Nullable final CommandSender notify) {
        final Assembler assembler = new Assembler(current.compression(), format, current.description());
        final List<Artifact> built = new ArrayList<>();
        final List<Assembler.Source> sources = new ArrayList<>();
        final Set<String> ids = new HashSet<>();
        for (final Layer layer : snapshot) {
            if (layer.enabled()) {
                built.add(buildLayer(assembler, current, layer, uniqueId(layer.name(), ids), sources));
            }
        }
        removeOrphans(ids);

        final Artifact result = assembler.build(sources.reversed(), workDir, combinedPack);
        try {
            Assembler.deleteRecursive(workDir);
        } catch (final IOException ignored) {
        }
        artifacts = List.copyOf(built);
        combined = result;
        lastBuildTime = LocalTime.now().format(TIME);

        result.log().forEach(line -> plugin.getLogger().info("[Pack] " + line));
        for (final Artifact artifact : built) {
            log(artifact);
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (notify != null) {
                report(notify, result, built);
            }
            if (result.ok()) {
                new BuiltEvent(combinedPack, result.sha1Hex(), result.sizeBytes(), sources.size()).callEvent();
            }
        });
    }

    private Artifact buildLayer(final Assembler assembler, final Settings current, final Layer layer,
                                    final String id, final List<Assembler.Source> sources) {
        final Assembler.Source source;
        try {
            source = new Assembler.Source(source(layer), layer.name());
        } catch (final IOException | RuntimeException e) {
            return Artifact.fail(id, layer.name(), "Quelle nicht verfügbar: " + e.getMessage(), List.of());
        }
        sources.add(source);
        final Artifact artifact = assembler.buildOne(source, id, workDir, packsDir.resolve(id + ".zip"));
        if (!artifact.ok() || artifact.file() == null) {
            return artifact;
        }
        if (!current.validate()) {
            return artifact.withValidation(new Artifact.Validation(List.of(), List.of("Testlauf deaktiviert.")));
        }
        return artifact.withValidation(Assembler.validate(artifact.file(), current.maxBytes()));
    }

    private void removeOrphans(final Set<String> ids) {
        try (Stream<Path> files = Files.list(packsDir)) {
            for (final Path file : files.toList()) {
                final String name = file.getFileName().toString();
                if (!name.toLowerCase(Locale.ROOT).endsWith(".zip")
                        || !ids.contains(name.substring(0, name.length() - 4))) {
                    Files.deleteIfExists(file);
                }
            }
        } catch (final IOException ignored) {
        }
    }

    private void log(final Artifact artifact) {
        if (!artifact.ok()) {
            plugin.getLogger().warning("[Pack] " + artifact.name() + ": " + artifact.message());
            return;
        }
        final Artifact.Validation validation = artifact.validation();
        final String line = "[Pack] " + artifact.name() + " (" + artifact.sizeHuman() + ", Format "
                + artifact.packFormat() + ", SHA1 " + artifact.sha1Short() + "): "
                + (validation == null ? "ungeprüft" : validation.summary());
        if (validation != null && !validation.ok()) {
            plugin.getLogger().warning(line);
        } else {
            plugin.getLogger().info(line);
        }
    }

    private void report(final CommandSender notify, final Artifact result, final List<Artifact> built) {
        final long ready = built.stream().filter(Artifact::sendable).count();
        notify.sendMessage(Component.text("[AethosRP] " + built.size() + " Pack(s) gebaut – " + ready + " sendbar."));
        for (final Artifact artifact : built) {
            if (artifact.sendable()) {
                continue;
            }
            final Artifact.Validation validation = artifact.validation();
            final String reason = !artifact.ok() || validation == null ? artifact.message() : validation.summary();
            notify.sendMessage(Component.text("  ✗ " + artifact.name() + ": " + reason));
        }
        notify.sendMessage(Component.text("[AethosRP] Kombiniertes Pack (nur intern): "
                + (result.ok() ? result.sizeHuman() + ", SHA1 " + result.sha1Short() : "Fehler – " + result.message())));
    }

    public record Layer(String name, String type, String ref, boolean enabled) {

        static final String UPLOAD = "upload";

        static final String PATH = "path";

        static final String PROVIDER = "provider";

        public boolean isUpload() {
            return UPLOAD.equalsIgnoreCase(type);
        }

        public boolean isProvider() {
            return PROVIDER.equalsIgnoreCase(type);
        }

        Layer withEnabled(final boolean value) {
            return new Layer(name, type, ref, value);
        }
    }

    public record Settings(boolean enabled, int port, String bind, String password, String publicUrl, boolean applyOnJoin,
                    boolean required, String prompt, String description, int startDelay, boolean compression,
                    boolean validate, boolean validateRequired, long maxBytes, boolean newUploadOnTop) {

        static final String CONFIG_PATH = "resourcepack.";

        static final Settings DISABLED = read(new MemoryConfiguration());

        static Settings read(final ConfigurationSection config) {
            return new Settings(
                    config.getBoolean(CONFIG_PATH + "enabled", false),
                    config.getInt(CONFIG_PATH + "web.port", 8124),
                    config.getString(CONFIG_PATH + "web.bind", "0.0.0.0"),
                    config.getString(CONFIG_PATH + "web.password", ""),
                    config.getString(CONFIG_PATH + "public-url", ""),
                    config.getBoolean(CONFIG_PATH + "apply-on-join", true),
                    config.getBoolean(CONFIG_PATH + "required", false),
                    config.getString(CONFIG_PATH + "prompt", "Server-Ressourcepack von Aethos"),
                    config.getString(CONFIG_PATH + "description", "Aethos Server Pack"),
                    config.getInt(CONFIG_PATH + "build-on-start-delay", 8),
                    config.getBoolean(CONFIG_PATH + "compression", true),
                    config.getBoolean(CONFIG_PATH + "validate.enabled", true),
                    config.getBoolean(CONFIG_PATH + "validate.required", true),
                    config.getLong(CONFIG_PATH + "validate.max-bytes", 100L * 1024 * 1024),
                    config.getBoolean(CONFIG_PATH + "new-upload-on-top", true));
        }

        public boolean hasPublicUrl() {
            return !publicUrl.isBlank();
        }

        public int uploadLimit() {
            final long limit = maxBytes > 0 ? maxBytes : 512L * 1024 * 1024;
            return (int) Math.min(limit, Integer.MAX_VALUE - 1);
        }
    }
}
