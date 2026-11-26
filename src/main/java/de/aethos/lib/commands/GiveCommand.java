package de.aethos.lib.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import de.aethos.lib.ItemRegistry;
import de.aethos.lib.commands.brigadier.BrigadierArguments;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GiveCommand {

    public static final int MAX_ALLOWED_ITEMSTACKS = 100;

    private static final Dynamic2CommandExceptionType TOO_MANY_ITEMS = new Dynamic2CommandExceptionType(
            (maxCount, item) -> {
                TranslatableComponent translatable = Component.translatable("commands.give.failed.toomanyitems",
                        Component.text((int) maxCount),
                        (ComponentLike) item);
                String plain = PlainTextComponentSerializer.plainText().serialize(translatable);
                return () -> plain;
            }
    );

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher, final ItemRegistry registry) {
        dispatcher.register(Commands.literal("give2")
                .requires(stack -> stack.getSender().hasPermission("de.aethos.admin"))
                .then(Commands.argument("targets", ArgumentTypes.players())
                        .then(Commands.argument("item", BrigadierArguments.ofRegistry(registry))
                                .executes(c -> giveItem(c,
                                        parserPlayers(c), 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(c -> giveItem(c,
                                                        parserPlayers(c),
                                                        IntegerArgumentType.getInteger(c, "count")
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.argument("item", BrigadierArguments.ofRegistry(registry))
                        .executes(c -> giveItem(c,
                                List.of(c.getSource().getPlayerOrThrow()), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                .executes(c -> giveItem(c,
                                                List.of(c.getSource().getPlayerOrThrow()),
                                                IntegerArgumentType.getInteger(c, "count")
                                        )
                                )
                        )
                )
        );
    }

    private static List<Player> parserPlayers(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getArgument("targets", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
    }

    private static int giveItem(final CommandContext<CommandSourceStack> context, final List<Player> players,
            final int count) throws CommandSyntaxException {
        final ItemStack item = context.getArgument("item", ItemRegistry.Item.class).getItemStack(count);
        final int maxStackSize = item.getMaxStackSize();
        final int maxAllowedCount = maxStackSize * MAX_ALLOWED_ITEMSTACKS;
        if (count > maxAllowedCount) {
            throw TOO_MANY_ITEMS.create(maxAllowedCount, item.displayName());
        }

        for (final Player player : players) {
            player.give(item);
        }
        final Component message;
        if (players.size() == 1) {
            message = Component.translatable(
                    "commands.give.success.single", Component.text(count), item.displayName(), players.getFirst().displayName()
            );
        } else {
            message = Component.translatable(
                    "commands.give.success.multiple", Component.text(count), item.displayName(), Component.text(players.size())
            );
        }
        context.getSource().getSender().sendMessage(message);
        return players.size();
    }
}
