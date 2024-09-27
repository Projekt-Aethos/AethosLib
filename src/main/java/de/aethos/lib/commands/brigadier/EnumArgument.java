package de.aethos.lib.commands.brigadier;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

import java.util.concurrent.CompletableFuture;

/**
 * Argument for an {@link Enum}.
 *
 * @param clazz the enum class
 * @param <E>   the enum type
 */
@SuppressWarnings("UnstableApiUsage")
public record EnumArgument<E extends Enum<E>>(Class<E> clazz) implements CustomArgumentType<E, String> {
    @Override
    public E parse(final StringReader reader) throws CommandSyntaxException {
        return Enum.valueOf(clazz, StringArgumentType.string().parse(reader));
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        for (final E value : clazz.getEnumConstants()) {
            builder.suggest(value.name());
        }
        return builder.buildFuture();
    }
}
