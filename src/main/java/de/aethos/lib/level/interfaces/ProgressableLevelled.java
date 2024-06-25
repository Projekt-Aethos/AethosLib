package de.aethos.lib.level.interfaces;

public interface ProgressableLevelled extends Levelled {
    void addProgress(double amount);

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
