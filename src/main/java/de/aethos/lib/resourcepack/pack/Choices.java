package de.aethos.lib.resourcepack.pack;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

final class Choices {

    private final Path file;

    private final Logger logger;

    private final Map<UUID, Set<String>> on = new ConcurrentHashMap<>();

    private final Map<UUID, Set<String>> off = new ConcurrentHashMap<>();

    Choices(final Path file, final Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    private static void read(final YamlConfiguration yaml, final String path, final Map<UUID, Set<String>> into) {
        final ConfigurationSection section = yaml.getConfigurationSection(path);
        if (section == null) {
            return;
        }
        for (final String key : section.getKeys(false)) {
            try {
                into.put(UUID.fromString(key), new LinkedHashSet<>(section.getStringList(key)));
            } catch (final IllegalArgumentException ignored) {
            }
        }
    }

    void load() {
        on.clear();
        off.clear();
        if (!Files.exists(file)) {
            return;
        }
        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        read(yaml, "players", on);
        read(yaml, "off", off);
    }

    boolean isOn(final UUID player, final String pack, final boolean defaultOn) {
        final Set<String> disabled = off.get(player);
        if (disabled != null && disabled.contains(pack)) {
            return false;
        }
        if (defaultOn) {
            return true;
        }
        final Set<String> enabled = on.get(player);
        return enabled != null && enabled.contains(pack);
    }

    boolean hasDecided(final UUID player) {
        return on.containsKey(player);
    }

    void markDecided(final UUID player) {
        if (on.putIfAbsent(player, new LinkedHashSet<>()) == null) {
            save();
        }
    }

    synchronized void set(final UUID player, final Map<String, Boolean> packs) {
        final Set<String> enabled = on.computeIfAbsent(player, key -> new LinkedHashSet<>());
        final Set<String> disabled = off.computeIfAbsent(player, key -> new LinkedHashSet<>());
        packs.forEach((pack, value) -> {
            if (value) {
                enabled.add(pack);
                disabled.remove(pack);
            } else {
                enabled.remove(pack);
                disabled.add(pack);
            }
        });
        if (disabled.isEmpty()) {
            off.remove(player);
        }
        save();
    }

    private synchronized void save() {
        final YamlConfiguration yaml = new YamlConfiguration();
        on.forEach((player, packs) -> yaml.set("players." + player, new ArrayList<>(packs)));
        off.forEach((player, packs) -> yaml.set("off." + player, new ArrayList<>(packs)));
        try {
            Files.createDirectories(file.getParent());
            yaml.save(file.toFile());
        } catch (final IOException e) {
            logger.warning(file.getFileName() + " konnte nicht gespeichert werden: " + e.getMessage());
        }
    }
}
