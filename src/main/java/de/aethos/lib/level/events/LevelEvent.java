package de.aethos.lib.level.events;

import de.aethos.lib.level.interfaces.Levelled;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class LevelEvent extends Event implements Cancellable {
    private final Levelled levelled;

    private boolean cancelled;

    public LevelEvent(@NotNull Levelled levelled) {
        this.levelled = levelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public @NotNull Levelled getLevelClass() {
        return levelled;
    }
}
