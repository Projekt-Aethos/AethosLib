package de.aethos.lib.level.events;

import de.aethos.lib.level.interfaces.Levelled;
import org.bukkit.event.HandlerList;

public class LevelUpEvent extends LevelEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final int oldLevel;

    private final int newLevel;

    public LevelUpEvent(final Levelled levelled, final int oldLevel, final int newLevel) {
        super(levelled);
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }
}
