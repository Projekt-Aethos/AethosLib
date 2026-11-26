package de.aethos.lib.xp.api;

import de.aethos.lib.xp.XPType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class XPGainEvent extends PlayerEvent implements Cancellable  {
    private static final HandlerList HANDLERS = new HandlerList();

    private final XPType type;
    private int amount;
    private boolean cancelled;

    public XPGainEvent(Player player, XPType type, int amount) {
        super(player);
        this.type = type;
        this.amount = amount;
    }

    public XPType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
