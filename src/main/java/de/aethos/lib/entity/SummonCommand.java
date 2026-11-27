package de.aethos.lib.entity;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.aethos.lib.hook.MythicMobsHook;
import de.aethos.lib.xp.XPType;
import de.aethos.lib.xp.api.XPManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public final class SummonCommand {

    public static final String XP_NAMESPACE = "aethos";

    public static final String XP_SUFFIX = "_experience_orb";

    public static final String MYTHIC_NAMESPACE = "mythicmobs";

    private static final DynamicCommandExceptionType UNKNOWN_ENTITY = new DynamicCommandExceptionType(
            id -> new LiteralMessage("Unbekannte Entity: " + id));

    private static final DynamicCommandExceptionType INVALID_NUMBER = new DynamicCommandExceptionType(
            value -> new LiteralMessage("Erwartet wird eine ganze Zahl ab 1, nicht: " + value));

    private static final SimpleCommandExceptionType MYTHIC_INACTIVE = new SimpleCommandExceptionType(
            new LiteralMessage("MythicMobs ist nicht aktiv."));

    private SummonCommand() {
    }

    public static void register(final Commands registrar, final XPManager xp) {
        registrar.register(Commands.literal("summon")
                .requires(source -> source.getSender().hasPermission("minecraft.command.summon"))
                .then(Commands.argument("entity", ArgumentTypes.namespacedKey())
                        .suggests(SummonCommand::suggest)
                        .executes(context -> summon(context, xp, context.getSource().getLocation(), ""))
                        .then(Commands.argument("pos", ArgumentTypes.finePosition(true))
                                .executes(context -> summon(context, xp, position(context), ""))
                                .then(Commands.argument("data", StringArgumentType.greedyString())
                                        .executes(context -> summon(context, xp, position(context),
                                                StringArgumentType.getString(context, "data"))))))
                .build());
    }

    private static boolean mythicActive() {
        return Bukkit.getPluginManager().isPluginEnabled(MythicMobsHook.PLUGIN);
    }

    private static CompletableFuture<Suggestions> suggest(final CommandContext<CommandSourceStack> context,
                                                          final SuggestionsBuilder builder) {
        for (final EntityType type : Registry.ENTITY_TYPE) {
            if (type != EntityType.PLAYER && type != EntityType.FISHING_BOBBER) {
                offer(builder, type.getKey().getNamespace(), type.getKey().getKey());
            }
        }
        for (final XPType type : XPType.values()) {
            offer(builder, XP_NAMESPACE, type.id() + XP_SUFFIX);
        }
        if (mythicActive()) {
            for (final String name : MythicMobsHook.mobNames()) {
                final String path = name.toLowerCase(Locale.ROOT);
                if (NamespacedKey.fromString(MYTHIC_NAMESPACE + ":" + path) != null) {
                    offer(builder, MYTHIC_NAMESPACE, path);
                }
            }
        }
        return builder.buildFuture();
    }

    private static void offer(final SuggestionsBuilder builder, final String namespace, final String path) {
        final String typed = builder.getRemainingLowerCase();
        final String id = namespace + ":" + path;
        if (id.startsWith(typed) || path.startsWith(typed)) {
            builder.suggest(id);
        }
    }

    private static Location position(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        return context.getArgument("pos", FinePositionResolver.class).resolve(source)
                .toLocation(source.getLocation().getWorld());
    }

    private static int summon(final CommandContext<CommandSourceStack> context, final XPManager xp,
                              final Location location, final String data) throws CommandSyntaxException {
        final NamespacedKey key = context.getArgument("entity", NamespacedKey.class);
        final CommandSender sender = context.getSource().getSender();
        if (key.getNamespace().equals(XP_NAMESPACE)) {
            final XPType type = key.getKey().endsWith(XP_SUFFIX)
                    ? XPType.byId(key.getKey().substring(0, key.getKey().length() - XP_SUFFIX.length())) : null;
            if (type == null) {
                throw UNKNOWN_ENTITY.create(key);
            }
            success(sender, xp.spawnOrb(location, type, number(data)));
            return Command.SINGLE_SUCCESS;
        }
        if (key.getNamespace().equals(MYTHIC_NAMESPACE)) {
            if (!mythicActive()) {
                throw MYTHIC_INACTIVE.create();
            }
            final Entity entity = MythicMobsHook.spawn(key.getKey(), location, number(data));
            if (entity == null) {
                throw UNKNOWN_ENTITY.create(key);
            }
            success(sender, entity);
            return Command.SINGLE_SUCCESS;
        }
        return vanilla(sender, key, location, data) ? Command.SINGLE_SUCCESS : 0;
    }

    private static int number(final String data) throws CommandSyntaxException {
        if (data.isBlank()) {
            return 1;
        }
        try {
            final int value = Integer.parseInt(data.trim());
            if (value >= 1) {
                return value;
            }
        } catch (final NumberFormatException ignored) {
        }
        throw INVALID_NUMBER.create(data.trim());
    }

    private static void success(final CommandSender sender, final Entity entity) {
        sender.sendMessage(Component.translatable("commands.summon.success", entity.name()));
    }

    private static boolean vanilla(final CommandSender sender, final NamespacedKey key, final Location location,
                                   final String data) {
        final World world = location.getWorld();
        final StringBuilder command = new StringBuilder();
        if (!sameWorld(sender, world)) {
            command.append("minecraft:execute in ").append(world.getKey()).append(" run ");
        }
        command.append("minecraft:summon ").append(key)
                .append(' ').append(plain(location.getX()))
                .append(' ').append(plain(location.getY()))
                .append(' ').append(plain(location.getZ()));
        if (!data.isBlank()) {
            command.append(' ').append(data.trim());
        }
        return Bukkit.dispatchCommand(sender, command.toString());
    }

    private static boolean sameWorld(final CommandSender sender, final World world) {
        if (sender instanceof final Entity entity) {
            return entity.getWorld().equals(world);
        }
        if (sender instanceof final BlockCommandSender block) {
            return block.getBlock().getWorld().equals(world);
        }
        return Bukkit.getWorlds().getFirst().equals(world);
    }

    private static String plain(final double value) {
        return BigDecimal.valueOf(value).toPlainString();
    }
}
