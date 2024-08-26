package de.aethos.lib.registry;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A Registry which has an enum with standard values for easy access.
 *
 * @param <T> being stored
 * @param <E> storing the standard values
 */
public class EnumRegistry<T extends Keyed, E extends Enum<E> & EnumRegistry.EnumHolderEnum<T>> extends Registry<T> {
    /**
     * The Enum class for access.
     */
    private final Class<E> enumClass;

    /**
     * Creates a new Registry with standard enum.
     *
     * @param enumClass to get the standard values from
     */
    public EnumRegistry(final JavaPlugin plugin, final Logger logger, final Class<E> enumClass) {
        super(plugin, logger);
        this.enumClass = enumClass;
    }

    /**
     * Creates a new Registry with standard enum.
     *
     * @param enumClass to get the standard values from
     * @param topic     to add after before the message
     */
    public EnumRegistry(final JavaPlugin plugin, final Logger logger, final Class<E> enumClass, @Nullable final String topic) {
        super(plugin, logger, topic);
        this.enumClass = enumClass;
    }

    /**
     * Gets a {@link T} with the given key.
     *
     * @param key to search for
     * @return null if no {@link T} found
     */
    @Override
    @Nullable
    public T get(final NamespacedKey key) {
        try {
            return Enum.valueOf(enumClass, key.getKey()).get();
        } catch (final IllegalArgumentException ignored) {
            return values.get(key);
        }
    }

    /**
     * Registers a new {@link T} in this Holder to gain by key and registers it as {@link Listener} if necessary.
     *
     * @param toRegister a new {@link T} with a different key from existing ones
     * @return if the newModifier could be registered successfully
     */
    @Override
    public boolean register(final T toRegister) {
        return Arrays.stream(enumClass.getEnumConstants()).map(EnumHolderEnum::get).map(Keyed::getKey)
                .noneMatch(key -> key.equals(toRegister.getKey()))
                && super.register(toRegister);
    }

    @Override
    public Set<T> getValues() {
        final Set<T> set = super.getValues();
        set.addAll(Arrays.stream(enumClass.getEnumConstants()).map(EnumHolderEnum::get).collect(Collectors.toSet()));
        return set;
    }

    @Override
    @Contract(pure = true)
    public Set<NamespacedKey> getKeys() {
        final Set<NamespacedKey> set = super.getKeys();
        set.addAll(Arrays.stream(enumClass.getEnumConstants()).map(EnumHolderEnum::get).map(Keyed::getKey).toList());
        return set;
    }

    /**
     * The interface for getting the stored value from the enum.
     *
     * @param <T> of the {@link EnumRegistry}
     */
    @FunctionalInterface
    protected interface EnumHolderEnum<T> {
        /**
         * Gets the value to store in this {@link Registry}.
         *
         * @return the {@link T} stored in the object
         */
        T get();
    }
}
