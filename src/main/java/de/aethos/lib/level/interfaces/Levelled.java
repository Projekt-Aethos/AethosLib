package de.aethos.lib.level.interfaces;

import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;

public interface Levelled extends Keyed {
    @NotNull LevelledHolder<?> getHolder();

    int getLevel();

    int getMaxLevel();
}
