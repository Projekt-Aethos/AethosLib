package de.aethos.lib.level;

import de.aethos.lib.level.interfaces.LevelClassFactory;
import de.aethos.lib.level.interfaces.LevelClassHolder;
import de.aethos.lib.registry.Registry;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class LevelApi {
    private final Map<UUID, LevelClassHolder<UUID>> HOLDER_MAP = new HashMap<>();

    private final Registry<LevelClassFactory> factoryRegistry;

    public LevelApi(@NotNull JavaPlugin plugin) {
        factoryRegistry = new Registry<>(plugin, plugin.getLogger());
    }

    public @Nullable LevelClassHolder<UUID> getExistingLevelClassHolder(@NotNull UUID uuid) {
        return HOLDER_MAP.get(uuid);
    }

    public @NotNull LevelClassHolder<UUID> getLevelClassHolder(@NotNull UUID uuid) {
        return HOLDER_MAP.computeIfAbsent(uuid, uuid1 -> new LevelClassHolderImpl<>(uuid1, factoryRegistry));
    }

    public void registerFactory(@NotNull LevelClassFactory factory) {
        factoryRegistry.register(factory);
    }

    public @NotNull Registry<LevelClassFactory> getFactoryRegistry() {
        return factoryRegistry;
    }
}
