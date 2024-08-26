package de.aethos.lib.level;

import de.aethos.lib.level.interfaces.Levelled;
import de.aethos.lib.level.interfaces.LevelledFactory;
import de.aethos.lib.level.interfaces.LevelledHolder;
import de.aethos.lib.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LevelledHolderImpl<ID> implements LevelledHolder<ID> {
    private final Map<NamespacedKey, Levelled> map = new HashMap<>();

    private final ID identifier;

    private final Registry<LevelledFactory> factories;

    public LevelledHolderImpl(final ID identifier, final Registry<LevelledFactory> Factories) {
        this.identifier = identifier;
        factories = Factories;
    }

    @Override
    public ID identifier() {
        return identifier;
    }

    @Override
    @Nullable
    public Levelled get(final NamespacedKey key, final boolean create) {
        final Levelled existing = map.get(key);
        if (existing != null || !create) {
            return existing;
        }

        final LevelledFactory factory = factories.get(key);
        if (factory == null) {
            throw new NoFactoryException(key);
        }

        final Levelled created = factory.create(this);
        map.put(key, created);
        return created;
    }
}
