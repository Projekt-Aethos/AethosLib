package de.ethos.ethoslib.inventory.item;

import org.bukkit.event.Event;
@Deprecated
@FunctionalInterface
public interface FunctionalEventHandler<T extends Event> {
    void onEvent(T t);
}
