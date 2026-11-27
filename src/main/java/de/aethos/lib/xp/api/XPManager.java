package de.aethos.lib.xp.api;

import de.aethos.lib.resourcepack.pack.Format;
import de.aethos.lib.xp.OrbService;
import de.aethos.lib.xp.OrbSprite;
import de.aethos.lib.xp.PackBuilder;
import de.aethos.lib.xp.XPType;
import de.aethos.lib.xp.api.listener.OrbMergeListener;
import de.aethos.lib.xp.api.listener.OrbPickupListener;
import de.aethos.lib.xp.api.listener.OrbSpawnListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class XPManager {

    public static final String PACK_FOLDER = "resourcepacks";
    public static final String PACK_FILE = "AethosXP.zip";
    private static final int FALLBACK_PACK_FORMAT = 69;
    private static final String CONFIG_PATH = "xp.";
    private static final int[] DEFAULT_MARKERS = {5, 100, 1500};
    private static final Material[] DEFAULT_ITEMS = {Material.SLIME_BALL, Material.RED_DYE, Material.BLUE_DYE};

    private final Plugin plugin;
    private final OrbService service;
    private final Map<XPType, NamespacedKey> pointKeys = new EnumMap<>(XPType.class);
    private boolean mergeEnabled = true;
    private BukkitTask mergeTask;

    public XPManager(Plugin plugin) {
        this.plugin = plugin;
        this.service = new OrbService(plugin);
        for (XPType type : XPType.values()) {
            pointKeys.put(type, new NamespacedKey(plugin, "points_" + type.id()));
        }
    }

    public void load(ConfigurationSection config) {
        try {
            Files.createDirectories(plugin.getDataFolder().toPath().resolve(PACK_FOLDER));
        } catch (IOException e) {
            plugin.getLogger().warning("Ordner " + PACK_FOLDER + " konnte nicht angelegt werden: " + e.getMessage());
        }
        Map<XPType, Integer> markers = new EnumMap<>(XPType.class);
        Map<XPType, Material> items = new EnumMap<>(XPType.class);
        Map<XPType, NamespacedKey> models = new EnumMap<>(XPType.class);
        for (XPType type : XPType.values()) {
            String path = CONFIG_PATH + "types." + type.id();
            markers.put(type, config.getInt(path + ".marker", DEFAULT_MARKERS[type.ordinal()]));
            Material material = Material.matchMaterial(config.getString(path + ".item", ""));
            if (material == null || !material.isItem()) {
                material = DEFAULT_ITEMS[type.ordinal()];
            }
            items.put(type, material);
            String modelName = config.getString(path + ".item-model", "");
            NamespacedKey model = modelName.isBlank() ? null : NamespacedKey.fromString(modelName);
            if (model != null) {
                models.put(type, model);
            }
        }
        List<String> problems = OrbSprite.validate(markers);
        problems.forEach(plugin.getLogger()::warning);
        if (problems.stream().anyMatch(problem -> problem.startsWith("FEHLER"))) {
            plugin.getLogger().severe("xp.types marker in config.yml ungültig – es gelten die Standardwerte 5 / 100 / 1500.");
            for (XPType type : XPType.values()) {
                markers.put(type, DEFAULT_MARKERS[type.ordinal()]);
            }
        }
        mergeEnabled = config.getBoolean(CONFIG_PATH + "merge.enabled", true);
        service.configure(markers,
                Math.max(0.1, config.getDouble(CONFIG_PATH + "merge.radius", 1.5)),
                Math.max(0, config.getInt(CONFIG_PATH + "merge.max-amount", 0)));
        service.removeDisplays(plugin.getServer().getWorlds(), false);
        service.configureDisplay(config.getBoolean(CONFIG_PATH + "display.enabled", true),
                (float) config.getDouble(CONFIG_PATH + "display.scale", 0.3),
                (float) config.getDouble(CONFIG_PATH + "display.y-offset", -0.325), items, models);

        if (mergeTask != null) {
            mergeTask.cancel();
        }
        long interval = Math.max(1, config.getInt(CONFIG_PATH + "merge.interval-ticks", 20));
        mergeTask = plugin.getServer().getScheduler().runTaskTimer(plugin,
                () -> service.mergeAll(plugin.getServer().getWorlds(), mergeEnabled), interval, interval);
    }

    public Path buildPack() throws IOException {
        Path folder = plugin.getDataFolder().toPath().resolve(PACK_FOLDER);
        Files.createDirectories(folder);
        BufferedImage source;
        Path custom = folder.resolve("experience_orb.png");
        if (Files.isRegularFile(custom)) {
            source = ImageIO.read(custom.toFile());
        } else {
            try (InputStream in = plugin.getResource("orb_source.png")) {
                source = in == null ? null : ImageIO.read(in);
            }
        }
        if (source == null) {
            throw new IOException("Keine lesbare Kugel-Textur gefunden");
        }
        ConfigurationSection config = plugin.getConfig();
        int[] format = packFormat(config);
        byte[] zip = PackBuilder.build(source, format,
                config.getString(CONFIG_PATH + "pack.description", "AethosXP - farbige XP-Kugeln"),
                Math.clamp(config.getDouble(CONFIG_PATH + "pack.opacity", 0.5), 0.0, 1.0));
        Path zipFile = folder.resolve(PACK_FILE);
        Path tmp = folder.resolve(PACK_FILE + ".tmp");
        Files.write(tmp, zip);
        Files.move(tmp, zipFile, StandardCopyOption.REPLACE_EXISTING);
        plugin.getLogger().info(PACK_FILE + " gebaut (Format " + format[0] + "." + format[1]
                + ", " + zip.length / 1024 + " KB, SHA-1 " + PackBuilder.sha1(zip) + ")");
        return zipFile;
    }

    private int[] packFormat(ConfigurationSection config) {
        String configured = config.getString(CONFIG_PATH + "pack.format", "auto").trim();
        if (!configured.isEmpty() && !configured.equalsIgnoreCase("auto")) {
            try {
                String[] parts = configured.split("\\.", 2);
                return new int[]{Integer.parseInt(parts[0]), parts.length > 1 ? Integer.parseInt(parts[1]) : 0};
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("xp.pack.format \"" + configured + "\" ungültig – wird automatisch ermittelt.");
            }
        }
        Format detected = Format.ofServer(plugin.getServer());
        if (detected != null) {
            return new int[]{detected.major(), detected.minor()};
        }
        plugin.getLogger().warning("Resource-Pack-Format unbekannt – bitte xp.pack.format in der config.yml setzen.");
        return new int[]{FALLBACK_PACK_FORMAT, 0};
    }

    public void registerListeners() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new OrbSpawnListener(plugin, service), plugin);
        pm.registerEvents(new OrbMergeListener(service, () -> mergeEnabled), plugin);
        pm.registerEvents(new OrbPickupListener(service, this), plugin);
    }

    public void shutdown() {
        if (mergeTask != null) {
            mergeTask.cancel();
            mergeTask = null;
        }
        service.removeDisplays(plugin.getServer().getWorlds(), false);
    }

    public void setMergeEnabled(boolean mergeEnabled) {
        this.mergeEnabled = mergeEnabled;
    }

    public ExperienceOrb spawnOrb(Location location, XPType type, int amount) {
        return service.spawn(location, type, amount);
    }

    public long getPoints(Player player, XPType type) {
        if (type == XPType.VANILLA) {
            return player.calculateTotalExperiencePoints();
        }
        Long value = player.getPersistentDataContainer().get(pointKeys.get(type), PersistentDataType.LONG);
        return value == null ? 0L : value;
    }

    public void setPoints(Player player, XPType type, long amount) {
        long points = Math.max(0L, amount);
        if (type == XPType.VANILLA) {
            player.setExperienceLevelAndProgress((int) Math.min(Integer.MAX_VALUE, points));
            return;
        }
        player.getPersistentDataContainer().set(pointKeys.get(type), PersistentDataType.LONG, points);
    }

    public void givePoints(Player player, XPType type, int amount) {
        if (amount <= 0) {
            return;
        }
        if (type == XPType.VANILLA) {
            player.giveExp(amount);
            return;
        }
        XPGainEvent gain = new XPGainEvent(player, type, amount);
        if (!gain.callEvent() || gain.getAmount() <= 0) {
            return;
        }
        setPoints(player, type, getPoints(player, type) + gain.getAmount());
    }

    public void takePoints(Player player, XPType type, int amount) {
        if (amount <= 0) {
            return;
        }
        if (type == XPType.VANILLA) {
            player.giveExp(-amount);
            return;
        }
        setPoints(player, type, getPoints(player, type) - amount);
    }

}
