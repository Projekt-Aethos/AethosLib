package de.aethos.lib.level.interfaces;

import org.bukkit.Keyed;

public interface LevelClass extends Keyed {
    int getLevel();

    int getMaxLevel();

    double getProgress();

    double getNeededProgress();

    /**
     * Gets the current progress to the next level.
     *
     * @return from 0.0 to 1.0
     */
    default double getRelativeProgress() {
        return getProgress() / getNeededProgress();
    }
}
