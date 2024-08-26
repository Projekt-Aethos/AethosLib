package de.aethos.lib.registry;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * A class for storing.
 *
 * @param <T> being stored
 */
public class Registry<T extends Keyed> {
    /**
     * Storage of {@link T}, mapped by unique key.
     */
    protected final Map<NamespacedKey, T> values;

    /**
     * The plugin used to register listener.
     */
    private final JavaPlugin plugin;

    /**
     * Used to log when values are un-/registered.
     */
    private final Logger logger;

    /**
     * To write before the message.
     */
    private final String prefix;

    /**
     * Creates a new empty holder.
     *
     * @param plugin to register listener with
     * @param logger to log new entries to
     */
    public Registry(final JavaPlugin plugin, final Logger logger) {
        this(plugin, logger, null);
    }

    /**
     * Creates a new empty holder.
     *
     * @param plugin to register listener with
     * @param logger to log new entries to
     * @param topic  to add after before the message
     */
    public Registry(final JavaPlugin plugin, final Logger logger, @Nullable final String topic) {
        this.plugin = plugin;
        this.logger = logger;
        this.values = new ConcurrentHashMap<>();
        this.prefix = "[" + getClass().getSimpleName() + (topic == null ? "" : " " + topic) + "] ";
    }

    /**
     * Gets a {@link T} with the given name.
     *
     * @param key to search for
     * @return null if no {@link T} found with that key
     */
    @Nullable
    public T get(final NamespacedKey key) {
        return values.get(key);
    }

    /**
     * Registers a new {@link T} in this Holder to gain by key and registers it as {@link Listener} if necessary.
     *
     * @param toRegister a new {@link T} with a different key from existing ones
     * @return if the newModifier could be registered successfully
     */
    public boolean register(final T toRegister) {
        final NamespacedKey key = toRegister.getKey();
        if (values.get(key) == null) {
            values.put(key, toRegister);
            if (toRegister instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) toRegister, plugin);
            }
            logger.info(prefix + "New registration: " + key + " (" + toRegister.getClass().getName() + ")");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unregisters a {@link T} from this Holder if present and unregisters it as {@link Listener} if necessary.
     *
     * @param key of the {@link T}
     * @return if a value is removed
     */
    public boolean deregister(final NamespacedKey key) {
        final T oldValue = values.remove(key);
        if (oldValue == null) {
            return false;
        }
        if (oldValue instanceof Listener) {
            HandlerList.unregisterAll((Listener) oldValue);
        }
        logger.info(prefix + "Unregistered: " + key + " (" + oldValue.getClass().getName() + ")");
        return true;
    }

    /**
     * Gets all stored values.
     *
     * @return stored values in a new Set
     */
    @Contract(pure = true)
    public Set<T> getValues() {
        return new HashSet<>(values.values());
    }

    /**
     * Gets the keys to all {@link T}.
     *
     * @return all keys to get {@link T} from
     */
    @Contract(pure = true)
    public Set<NamespacedKey> getKeys() {
        return new HashSet<>(values.keySet());
    }
}
