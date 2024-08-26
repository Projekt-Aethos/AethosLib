package de.aethos.lib.level.interfaces;

import org.bukkit.Keyed;

public interface Levelled extends Keyed {
    LevelledHolder<?> getHolder();

    int getLevel();

    int getMaxLevel();
}
