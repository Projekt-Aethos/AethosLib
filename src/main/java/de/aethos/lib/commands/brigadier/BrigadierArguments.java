package de.aethos.lib.commands.brigadier;

import de.aethos.lib.registry.Registry;
import org.bukkit.Keyed;

/**
 * Custom commonly used {@link com.mojang.brigadier.arguments.ArgumentType}s.
 */
@SuppressWarnings("unused")
public interface BrigadierArguments {
    static <E extends Enum<E>> EnumArgument<E> ofEnum(final Class<E> clazz) {
        return new EnumArgument<>(clazz);
    }

    static <T extends Keyed> RegistryArgument<T> ofRegistry(final Registry<T> registry) {
        return new RegistryArgument<>(registry);
    }
}
