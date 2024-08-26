package de.aethos.lib.level.interfaces;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

public interface LevelledFactory extends Keyed {
    Levelled create(LevelledHolder<?> holder);

    /**
     * Unique key to identify with LevelClass creation.
     *
     * @return the same key as the constructed LevelClass
     */
    @Override
    NamespacedKey getKey();
}
