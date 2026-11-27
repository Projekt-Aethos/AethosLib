package de.aethos.lib.resourcepack;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.aethos.lib.resourcepack.ResourcePackManager.Layer;
import de.aethos.lib.resourcepack.pack.Artifact;
import de.aethos.lib.resourcepack.pack.Sender;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public final class ResourcePackCommand {

    private static final String PREFIX = "[AethosRP] ";

    private ResourcePackCommand() {
    }

    public static void register(final Commands registrar, final ResourcePackManager manager) {
        registrar.register(Commands.literal("aethosrp")
                .requires(source -> source.getSender().hasPermission("de.aethos.admin"))
                .then(Commands.literal("build").executes(context -> build(context, manager)))
                .then(Commands.literal("test").executes(context -> build(context, manager)))
                .then(Commands.literal("send")
                        .executes(context -> send(context, manager, Bukkit.getOnlinePlayers()))
                        .then(Commands.argument("targets", ArgumentTypes.players())
                                .executes(context -> send(context, manager, targets(context)))))
                .then(Commands.literal("packs").executes(context -> packs(context, manager)))
                .then(layer("up", manager))
                .then(layer("down", manager))
                .then(layer("enable", manager))
                .then(layer("disable", manager))
                .then(layer("optional", manager))
                .then(layer("default", manager))
                .then(Commands.literal("info").executes(context -> info(context, manager)))
                .then(Commands.literal("list").executes(context -> list(context, manager)))
                .then(Commands.literal("link").executes(context -> link(context, manager)))
                .then(Commands.literal("token").executes(context -> token(context, manager)))
                .then(Commands.literal("reload").executes(context -> reload(context, manager)))
                .build(), "Server-Ressourcepack verwalten");
        registrar.register(Commands.literal("pack")
                .executes(context -> choose(context, manager, false))
                .then(Commands.literal("liste").executes(context -> choose(context, manager, true)))
                .then(toggle("an", manager, true))
                .then(toggle("aus", manager, false))
                .build(), "Optionale Ressourcepacks ein- und ausschalten", List.of("ressourcepack"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> layer(final String action, final ResourcePackManager manager) {
        return Commands.literal(action)
                .then(Commands.argument("ref", StringArgumentType.greedyString())
                        .suggests((context, builder) -> {
                            for (final Layer layer : manager.layers()) {
                                if (layer.ref().toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase())) {
                                    builder.suggest(layer.ref());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> layer(context, manager, action)));
    }

    private static List<Player> targets(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return context.getArgument("targets", PlayerSelectorArgumentResolver.class).resolve(context.getSource());
    }

    private static boolean inactive(final CommandSender sender, final ResourcePackManager manager) {
        if (manager.isEnabled()) {
            return false;
        }
        sender.sendMessage(Component.text(PREFIX + "Deaktiviert – resourcepack.enabled in der config.yml setzen, "
                + "dann /aethosrp reload.", NamedTextColor.RED));
        return true;
    }

    private static void message(final CommandSender sender, final String text) {
        sender.sendMessage(Component.text(text));
    }

    private static int build(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager) {
        final CommandSender sender = context.getSource().getSender();
        if (inactive(sender, manager)) {
            return 0;
        }
        if (!manager.rebuild(sender)) {
            message(sender, PREFIX + "Build läuft bereits …");
            return 0;
        }
        message(sender, PREFIX + "Baue Packs + Testlauf …");
        return Command.SINGLE_SUCCESS;
    }

    private static int send(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager,
                            final Collection<? extends Player> players) {
        final CommandSender sender = context.getSource().getSender();
        if (inactive(sender, manager)) {
            return 0;
        }
        if (manager.artifacts().isEmpty()) {
            message(sender, PREFIX + "Keine gebauten Packs – erst /aethosrp build.");
            return 0;
        }
        final int packs = manager.sendable().size();
        if (packs == 0) {
            message(sender, PREFIX + "Kein Pack hat den Testlauf bestanden – siehe /aethosrp info.");
            return 0;
        }
        int reached = 0;
        for (final Player player : players) {
            if (manager.send(player) > 0) {
                reached++;
            }
        }
        message(sender, PREFIX + packs + " Pack(s) an " + reached + " Spieler gesendet.");
        return reached;
    }

    private static int packs(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager) {
        final CommandSender sender = context.getSource().getSender();
        if (inactive(sender, manager)) {
            return 0;
        }
        manager.syncLayers();
        final List<Layer> layers = manager.layers();
        message(sender, PREFIX + "Pack-Priorität (oben = höchste, überschreibt darunter):");
        if (layers.isEmpty()) {
            message(sender, "  (leer – Packs hochladen oder in der config.yml eintragen)");
        }
        for (int i = 0; i < layers.size(); i++) {
            final Layer layer = layers.get(i);
            message(sender, "  " + (i + 1) + ". " + (layer.enabled() ? "■" : "□") + " " + layer.name()
                    + " [" + layer.type() + ": " + layer.ref() + "]"
                    + (layer.optional() ? layer.defaultOn() ? " (optional, Standard an)" : " (optional)" : "")
                    + (layer.follows() == null ? "" : " (folgt " + layer.follows() + ")") + (manager.layerPresent(layer) ? "" : " (fehlt)"));
        }
        return layers.size();
    }

    private static int layer(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager,
                             final String action) {
        final CommandSender sender = context.getSource().getSender();
        if (inactive(sender, manager)) {
            return 0;
        }
        final String ref = StringArgumentType.getString(context, "ref");
        final Layer layer = manager.findLayer(ref);
        if (layer == null) {
            message(sender, PREFIX + "Layer nicht gefunden: " + ref);
            return 0;
        }
        switch (action) {
            case "up" -> message(sender, PREFIX + (manager.moveLayer(layer.ref(), -1)
                    ? "'" + layer.name() + "' höher priorisiert." : "'" + layer.name() + "' ist bereits ganz oben."));
            case "down" -> message(sender, PREFIX + (manager.moveLayer(layer.ref(), 1)
                    ? "'" + layer.name() + "' niedriger priorisiert." : "'" + layer.name() + "' ist bereits ganz unten."));
            case "optional" -> {
                manager.toggleOptional(layer.ref());
                message(sender, PREFIX + "Layer '" + layer.name() + "' ist jetzt "
                        + (layer.optional() ? "Pflicht (geht an alle)" : "optional (Spieler wählen per /pack)")
                        + ". Danach /aethosrp build und /aethosrp send.");
            }
            case "default" -> {
                if (!layer.optional()) {
                    message(sender, PREFIX + "Layer '" + layer.name() + "' ist Pflicht – erst /aethosrp optional " + layer.ref() + ".");
                    return 0;
                }
                manager.toggleDefaultOn(layer.ref());
                message(sender, PREFIX + "Optionales Pack '" + layer.name() + "' ist jetzt standardmäßig "
                        + (layer.defaultOn() ? "aus" : "an") + ". Danach /aethosrp build und /aethosrp send.");
            }
            default -> {
                final boolean enable = action.equals("enable");
                manager.setLayerEnabled(layer.ref(), enable);
                message(sender, PREFIX + "Layer '" + layer.name() + "' ist jetzt " + (enable ? "aktiv" : "inaktiv") + ".");
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int info(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager) {
        final CommandSender sender = context.getSource().getSender();
        if (inactive(sender, manager)) {
            return 0;
        }
        final ResourcePackManager.Settings settings = manager.settings();
        message(sender, PREFIX + "Basis-URL: " + manager.baseUrl()
                + " | Kompression " + (settings.compression() ? "an" : "aus")
                + ", Testlauf " + (settings.validate() ? settings.validateRequired() ? "an (Pflicht)" : "an (Hinweis)" : "aus")
                + " | akzeptiert/abgelehnt/Fehler " + manager.sender().counts());
        final List<Artifact> artifacts = manager.artifacts();
        if (artifacts.isEmpty()) {
            message(sender, PREFIX + "Noch nicht gebaut – /aethosrp build.");
            return 0;
        }
        message(sender, PREFIX + "Einzel-Packs (oben = höchste Priorität), gebaut " + manager.lastBuildTime() + ":");
        for (final Artifact artifact : artifacts) {
            final Artifact.Validation validation = artifact.validation();
            final String detail = !artifact.ok() ? artifact.message()
                    : artifact.sizeHuman() + ", Format " + artifact.packFormat() + ", SHA1 " + artifact.sha1Short()
                    + " – " + (validation == null ? "ungeprüft" : validation.summary());
            message(sender, "  " + (artifact.sendable() ? "✓" : "✗") + (manager.sender().isOptional(artifact) ? " (optional) " : " ") + artifact.name() + ": " + detail);
        }
        final Artifact combined = manager.combined();
        message(sender, PREFIX + "Kombiniert (nur intern): " + (combined != null && combined.ok()
                ? combined.sizeHuman() + ", " + combined.entryCount() + " Einträge, SHA1 " + combined.sha1Short() : "-"));
        return artifacts.size();
    }

    private static int list(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager) {
        final CommandSender sender = context.getSource().getSender();
        if (inactive(sender, manager)) {
            return 0;
        }
        final List<String> uploads = manager.uploads();
        message(sender, PREFIX + "Uploads (" + uploads.size() + "):");
        uploads.forEach(upload -> message(sender, "  • " + upload));
        return uploads.size();
    }

    private static int link(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager) {
        final CommandSender sender = context.getSource().getSender();
        if (inactive(sender, manager)) {
            return 0;
        }
        final String url = manager.websiteUrl();
        sender.sendMessage(Component.text("Resourcepack-Verwaltung im Browser: ", NamedTextColor.GOLD)
                .append(Component.text(url, NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl(url))));
        return Command.SINGLE_SUCCESS;
    }

    private static int token(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager) {
        final CommandSender sender = context.getSource().getSender();
        if (inactive(sender, manager)) {
            return 0;
        }
        final String url = manager.loginUrl();
        if (url == null) {
            sender.sendMessage(Component.text(PREFIX + "Der Web-Server läuft nicht.", NamedTextColor.RED));
            return 0;
        }
        sender.sendMessage(Component.text("Einmal-Login für die Resourcepack-Verwaltung (5 Minuten gültig): ", NamedTextColor.GOLD)
                .append(Component.text("[öffnen]", NamedTextColor.AQUA).clickEvent(ClickEvent.openUrl(url))));
        return Command.SINGLE_SUCCESS;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> toggle(final String name, final ResourcePackManager manager,
                                                                    final boolean on) {
        return Commands.literal(name)
                .then(Commands.argument("pack", StringArgumentType.greedyString())
                        .suggests((context, builder) -> {
                            for (final Artifact artifact : manager.sender().optional()) {
                                if (artifact.id().startsWith(builder.getRemainingLowerCase())) {
                                    builder.suggest(artifact.id());
                                }
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> toggle(context, manager, on)));
    }

    private static int choose(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager,
                              final boolean asText) {
        if (!(context.getSource().getSender() instanceof final Player player)) {
            message(context.getSource().getSender(), PREFIX + "/pack geht nur als Spieler.");
            return 0;
        }
        final Sender packs = manager.sender();
        packs.markDecided(player);
        final List<Artifact> optional = packs.optional();
        if (optional.isEmpty()) {
            player.sendMessage(Component.text("Zurzeit gibt es keine optionalen Ressourcepacks.", NamedTextColor.GRAY));
            return 0;
        }
        if (!asText) {
            packs.openMenu(player);
            return Command.SINGLE_SUCCESS;
        }
        player.sendMessage(Component.text("Optionale Ressourcepacks (Klick schaltet um):", NamedTextColor.GOLD));
        for (final Artifact artifact : optional) {
            final boolean on = packs.wants(player.getUniqueId(), artifact);
            player.sendMessage(Component.text("  ")
                    .append(Component.text(on ? "[an] " : "[aus] ", on ? NamedTextColor.GREEN : NamedTextColor.RED))
                    .append(Component.text(artifact.name(), NamedTextColor.WHITE))
                    .append(Component.text(" (" + artifact.sizeHuman() + ")", NamedTextColor.GRAY))
                    .clickEvent(ClickEvent.runCommand("/pack " + (on ? "aus " : "an ") + artifact.id())));
        }
        return optional.size();
    }

    private static int toggle(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager,
                              final boolean on) {
        if (!(context.getSource().getSender() instanceof final Player player)) {
            message(context.getSource().getSender(), PREFIX + "/pack geht nur als Spieler.");
            return 0;
        }
        final Sender packs = manager.sender();
        packs.markDecided(player);
        final String wanted = StringArgumentType.getString(context, "pack");
        final Artifact artifact = packs.optional().stream()
                .filter(candidate -> candidate.id().equalsIgnoreCase(wanted) || candidate.name().equalsIgnoreCase(wanted))
                .findFirst().orElse(null);
        if (artifact == null) {
            player.sendMessage(Component.text("Kein optionales Pack mit dem Namen '" + wanted + "' – siehe /pack.",
                    NamedTextColor.RED));
            return 0;
        }
        return packs.choose(player, Map.of(artifact.id(), on));
    }

    private static int reload(final CommandContext<CommandSourceStack> context, final ResourcePackManager manager) {
        manager.reload();
        message(context.getSource().getSender(), PREFIX + "Konfiguration neu geladen – Resourcepack "
                + (manager.isEnabled() ? "aktiv" : "deaktiviert") + ".");
        return Command.SINGLE_SUCCESS;
    }
}
