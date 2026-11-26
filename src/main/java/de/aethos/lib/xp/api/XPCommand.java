package de.aethos.lib.xp.api;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.aethos.lib.xp.XPType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class XPCommand {

    private static final SimpleCommandExceptionType SET_POINTS_INVALID = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.translatable("commands.experience.set.points.invalid")));

    private static final DynamicCommandExceptionType UNKNOWN_TYPE = new DynamicCommandExceptionType(
            id -> new LiteralMessage("Unbekannte XP-Art: " + id));

    private XPCommand() {
    }

    public static void register(final Commands registrar, final XPManager manager) {
        registrar.register(build("xp", manager));
        registrar.register(build("experience", manager));
    }

    private static LiteralCommandNode<CommandSourceStack> build(final String label, final XPManager manager) {
        return Commands.literal(label)
                .requires(source -> source.getSender().hasPermission("minecraft.command." + label))
                .then(change("add", IntegerArgumentType.integer(), manager, false))
                .then(change("set", IntegerArgumentType.integer(0), manager, true))
                .then(Commands.literal("query")
                        .then(Commands.argument("target", ArgumentTypes.player())
                                .then(Commands.literal("points")
                                        .executes(context -> queryPoints(context, manager, XPType.VANILLA))
                                        .then(typeArgument()
                                                .executes(context -> queryPoints(context, manager, type(context)))))
                                .then(Commands.literal("levels")
                                        .executes(XPCommand::queryLevels))))
                .build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> change(final String name, final ArgumentType<Integer> amount,
                                                                     final XPManager manager, final boolean set) {
        return Commands.literal(name)
                .then(Commands.argument("targets", ArgumentTypes.players())
                        .then(Commands.argument("amount", amount)
                                .executes(context -> points(context, manager, XPType.VANILLA, set))
                                .then(Commands.literal("points")
                                        .executes(context -> points(context, manager, XPType.VANILLA, set))
                                        .then(typeArgument()
                                                .executes(context -> points(context, manager, type(context), set))))
                                .then(Commands.literal("levels")
                                        .executes(context -> levels(context, set)))));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> typeArgument() {
        return Commands.argument("type", StringArgumentType.word()).suggests((context, builder) -> {
            for (final XPType type : XPType.values()) {
                if (type.id().startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(type.id());
                }
            }
            return builder.buildFuture();
        });
    }

    private static XPType type(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final String id = StringArgumentType.getString(context, "type");
        final XPType type = XPType.byId(id);
        if (type == null) {
            throw UNKNOWN_TYPE.create(id);
        }
        return type;
    }

    private static List<Player> targets(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getArgument("targets", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
    }

    private static int points(final CommandContext<CommandSourceStack> context, final XPManager manager,
                              final XPType type, final boolean set) throws CommandSyntaxException {
        final List<Player> targets = targets(context);
        final int amount = IntegerArgumentType.getInteger(context, "amount");
        if (type == XPType.VANILLA) {
            final int changed = vanillaPoints(targets, amount, set);
            feedback(context, set ? "set" : "add", "points", amount, targets);
            return changed;
        }
        for (final Player player : targets) {
            if (set) {
                manager.setPoints(player, type, amount);
            } else if (amount >= 0) {
                manager.givePoints(player, type, amount);
            } else {
                manager.takePoints(player, type, -amount);
            }
        }
        final String target = targets.size() == 1 ? targets.getFirst().getName() : targets.size() + " Spieler";
        context.getSource().getSender().sendMessage(Component.text(
                type.id() + "-XP " + (set ? "auf " + amount + " gesetzt für " : (amount >= 0 ? "+" : "") + amount + " für ") + target));
        return targets.size();
    }

    private static int vanillaPoints(final List<Player> targets, final int amount, final boolean set)
            throws CommandSyntaxException {
        if (!set) {
            targets.forEach(player -> player.giveExp(amount));
            return targets.size();
        }
        int changed = 0;
        for (final Player player : targets) {
            final int needed = player.getExpToLevel();
            if (amount < needed) {
                player.setExp(amount / (float) needed);
                changed++;
            }
        }
        if (changed == 0) {
            throw SET_POINTS_INVALID.create();
        }
        return changed;
    }

    private static int levels(final CommandContext<CommandSourceStack> context, final boolean set) throws CommandSyntaxException {
        final List<Player> targets = targets(context);
        final int amount = IntegerArgumentType.getInteger(context, "amount");
        for (final Player player : targets) {
            if (set) {
                player.setLevel(amount);
            } else {
                player.giveExpLevels(amount);
            }
        }
        feedback(context, set ? "set" : "add", "levels", amount, targets);
        return targets.size();
    }

    private static void feedback(final CommandContext<CommandSourceStack> context, final String action, final String unit,
                                 final int amount, final List<Player> targets) {
        final String key = "commands.experience." + action + "." + unit + ".success.";
        final Component message = targets.size() == 1
                ? Component.translatable(key + "single", Component.text(amount), targets.getFirst().displayName())
                : Component.translatable(key + "multiple", Component.text(amount), Component.text(targets.size()));
        context.getSource().getSender().sendMessage(message);
    }

    private static Player target(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
    }

    private static int queryPoints(final CommandContext<CommandSourceStack> context, final XPManager manager,
                                   final XPType type) throws CommandSyntaxException {
        final Player player = target(context);
        if (type == XPType.VANILLA) {
            final int points = Math.round(player.getExp() * player.getExpToLevel());
            context.getSource().getSender().sendMessage(Component.translatable("commands.experience.query.points",
                    player.displayName(), Component.text(points)));
            return points;
        }
        final long points = manager.getPoints(player, type);
        context.getSource().getSender().sendMessage(Component.text(player.getName() + " hat " + points + " " + type.id() + "-XP"));
        return (int) Math.min(Integer.MAX_VALUE, points);
    }

    private static int queryLevels(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final Player player = target(context);
        context.getSource().getSender().sendMessage(Component.translatable("commands.experience.query.levels",
                player.displayName(), Component.text(player.getLevel())));
        return player.getLevel();
    }
}
