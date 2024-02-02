package de.aethos.lib.registry;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * Stores more specific values both in parent registry and itself.
 *
 * @param <T> to store, extending U
 * @param <U> to which T is more specific
 */
public class SubRegistry<T extends U, U extends Keyed> extends Registry<T> {
    private final Registry<U> parentRegistry;

    public SubRegistry(@NotNull JavaPlugin plugin, @NotNull Logger logger, @Nullable String topic, @NotNull Registry<U> parent) {
        super(plugin, logger, topic);
        this.parentRegistry = parent;
    }

    @Override
    public boolean register(@NotNull T toRegister) {
        if (parentRegistry.register(toRegister)) {
            if (super.register(toRegister)) {
                return true;
            }
            parentRegistry.deregister(toRegister.getKey());
            return false;
        }
        return false;
    }

    @Override
    public boolean deregister(@NotNull NamespacedKey key) {
        if (super.deregister(key)) {
            parentRegistry.deregister(key);
            return true;
        }
        return false;
    }
}