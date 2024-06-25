package de.aethos.lib.level.interfaces;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public interface LevelledFactory extends Keyed {
    @NotNull Levelled create(@NotNull LevelledHolder<?> holder);

    /**
     * Unique key to identify with LevelClass creation.
     *
     * @return the same key as the constructed LevelClass
     */
    @Override
    @NotNull NamespacedKey getKey();
}
