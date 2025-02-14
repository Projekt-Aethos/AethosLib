package de.aethos.lib.registry;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * Stores more specific values both in parent registry and itself.
 *
 * @param <T> to store, extending U
 * @param <U> to which T is more specific
 */
public class SubRegistry<T extends U, U extends Keyed> extends Registry<T> {
    /**
     * Registry where all modification will be reflected and mirrored.
     */
    private final Registry<U> parentRegistry;

    /**
     * Creates a new sub registry.
     * <p>
     * De-/Registration of entries will reflect in the parent registry.
     *
     * @param plugin to register listener with
     * @param logger to log entry modifications to
     * @param topic  to add after before the message
     * @param parent to also store entries to
     */
    public SubRegistry(final JavaPlugin plugin, final Logger logger, @Nullable final String topic, final Registry<U> parent) {
        super(plugin, logger, topic);
        this.parentRegistry = parent;
    }

    @Override
    public boolean register(final T toRegister) {
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
    public boolean deregister(final NamespacedKey key) {
        if (super.deregister(key)) {
            parentRegistry.deregister(key);
            return true;
        }
        return false;
    }
}
