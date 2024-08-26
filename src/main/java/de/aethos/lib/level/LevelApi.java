package de.aethos.lib.level;

import de.aethos.lib.level.interfaces.LevelledFactory;
import de.aethos.lib.level.interfaces.LevelledHolder;
import de.aethos.lib.registry.Registry;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class LevelApi {
    private final Map<UUID, LevelledHolder<UUID>> HOLDER_MAP = new HashMap<>();

    private final Registry<LevelledFactory> factoryRegistry;

    public LevelApi(final JavaPlugin plugin) {
        factoryRegistry = new Registry<>(plugin, plugin.getLogger());
    }

    @Nullable
    public LevelledHolder<UUID> getExistingLevelClassHolder(final UUID uuid) {
        return HOLDER_MAP.get(uuid);
    }

    public LevelledHolder<UUID> getLevelClassHolder(final UUID uuid) {
        return HOLDER_MAP.computeIfAbsent(uuid, uuid1 -> new LevelledHolderImpl<>(uuid1, factoryRegistry));
    }

    public void registerFactory(final LevelledFactory factory) {
        factoryRegistry.register(factory);
    }

    public Registry<LevelledFactory> getFactoryRegistry() {
        return factoryRegistry;
    }
}
