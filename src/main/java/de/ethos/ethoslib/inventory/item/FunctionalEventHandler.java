package de.ethos.ethoslib.inventory.item;

import org.bukkit.event.Event;
@FunctionalInterface
public interface FunctionalEventHandler<T extends Event> {
    void onEvent(T t);
}
