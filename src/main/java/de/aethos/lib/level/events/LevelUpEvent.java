package de.aethos.lib.level.events;

import de.aethos.lib.level.interfaces.Levelled;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LevelUpEvent extends LevelEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final int oldLevel;

    private final int newLevel;

    public LevelUpEvent(@NotNull Levelled levelled, int oldLevel, int newLevel) {
        super(levelled);
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

}
