package de.aethos.lib.level.events;

import de.aethos.lib.level.interfaces.LevelClass;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class LevelEvent extends Event implements Cancellable {
    private final LevelClass levelClass;

    private boolean cancelled;

    public LevelEvent(@NotNull LevelClass levelClass) {
        this.levelClass = levelClass;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public @NotNull LevelClass getLevelClass() {
        return levelClass;
    }
}
