package de.aethos.lib.level;

import de.aethos.lib.level.interfaces.LevelClass;
import de.aethos.lib.level.interfaces.LevelClassFactory;
import de.aethos.lib.level.interfaces.LevelClassHolder;
import de.aethos.lib.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LevelClassHolderImpl<ID> implements LevelClassHolder<ID> {
    private final Map<NamespacedKey, LevelClass> map = new HashMap<>();

    private final ID identifier;

    private final Registry<LevelClassFactory> factories;

    public LevelClassHolderImpl(@NotNull ID identifier, @NotNull Registry<LevelClassFactory> Factories) {
        this.identifier = identifier;
        factories = Factories;
    }

    @Override
    public @NotNull ID identifier() {
        return identifier;
    }

    @Override
    public @Nullable LevelClass get(@NotNull NamespacedKey key, boolean create) {
        LevelClass existing = map.get(key);
        if (existing != null || !create) {
            return existing;
        }

        LevelClassFactory factory = factories.get(key);
        if (factory == null) {
            throw new NoFactoryException(key);
        }

        LevelClass created = factory.create();
        map.put(key, created);
        return created;
    }
}
