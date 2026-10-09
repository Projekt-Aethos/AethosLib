package de.aethos.lib.registry;

import org.bukkit.Keyed;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.logging.Logger;

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
     * @param plugin    to register listener with
     * @param logger    to log entry modifications to
     * @param enumClass to get the standard values from
     */
    public EnumRegistry(final JavaPlugin plugin, final Logger logger, final Class<E> enumClass) {
        this(plugin, logger, enumClass, null);
    }

    /**
     * Creates a new Registry with standard enum.
     *
     * @param plugin    to register listener with
     * @param logger    to log entry modifications to
     * @param enumClass to get the standard values from
     * @param topic     to add after before the message
     */
    public EnumRegistry(final JavaPlugin plugin, final Logger logger, final Class<E> enumClass, @Nullable final String topic) {
        super(plugin, logger, topic);
        this.enumClass = enumClass;
        for (final E value : enumClass.getEnumConstants()) {
            if (!super.register(value.get())) {
                throw new RuntimeException("Can't use enum (%s) that contains same key (%s) for multiple values!"
                        .formatted(value, value.get().getKey()));
            }
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
