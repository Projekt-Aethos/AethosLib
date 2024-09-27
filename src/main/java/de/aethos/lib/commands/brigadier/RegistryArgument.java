package de.aethos.lib.commands.brigadier;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.aethos.lib.registry.Registry;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.concurrent.CompletableFuture;

/**
 * Argument for a {@link Registry}.
 *
 * @param registry the registry providing {@link T}s.
 * @param <T>      the type stored in the registry
 */
@SuppressWarnings("UnstableApiUsage")
public record RegistryArgument<T extends Keyed>(Registry<T> registry) implements CustomArgumentType<T, NamespacedKey> {
    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {
        final NamespacedKey key = ArgumentTypes.namespacedKey().parse(reader);
        final T type = registry.get(key);
        if (type == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(key + " does not exist!");
        }
        return type;
    }

    @Override
    public ArgumentType<NamespacedKey> getNativeType() {
        return ArgumentTypes.namespacedKey();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (final NamespacedKey key : registry.getKeys()) {
            builder.suggest(key.toString());
        }
        return builder.buildFuture();
    }
}
