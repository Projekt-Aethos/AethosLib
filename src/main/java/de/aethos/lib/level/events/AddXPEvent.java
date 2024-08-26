package de.aethos.lib.level.events;

import de.aethos.lib.level.interfaces.Levelled;
import org.bukkit.event.HandlerList;

import java.util.LinkedList;
import java.util.List;

public class AddXPEvent extends LevelEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final double initialValue;

    private final List<Double> baseMultiplier;

    private final List<Double> wholeMultiplier;

    private boolean cached;

    private double cachedValue;

    public AddXPEvent(final Levelled levelled, final double amount) {
        super(levelled);
        this.initialValue = amount;
        this.baseMultiplier = new LinkedList<>();
        this.wholeMultiplier = new LinkedList<>();
    }

    /**
     * Adds a multiplier to the base value.
     * <p>
     * This will result in base * (1.5 + 1.5).
     *
     * @param value to multiply the base value with
     */
    public void addBaseMultiply(final double value) {
        cached = false;
        baseMultiplier.add(value);
    }

    /**
     * Adds a multiplier to the overall value.
     * <p>
     * This will result in base * 1.5 * 1.5.
     *
     * @param value to multiply the value with
     */
    public void addWholeMultiply(final double value) {
        cached = false;
        wholeMultiplier.add(value);
    }

    public double getInitialValue() {
        return initialValue;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public double getFinalXP() {
        if (!cached) {
            final double baseM = baseMultiplier.stream().mapToDouble(a -> a).sum();
            double midValue = initialValue * (1 + baseM);
            for (final double value : wholeMultiplier) {
                midValue *= value;
            }
            cachedValue = midValue;
            cached = true;
        }
        return cachedValue;
    }
}
