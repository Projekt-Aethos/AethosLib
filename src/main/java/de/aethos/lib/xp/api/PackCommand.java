package de.aethos.lib.xp.api;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("UnstableApiUsage")
public final class PackCommand {

    private PackCommand() {
    }

    public static void register(final Commands registrar, final XPManager manager) {
        registrar.register(Commands.literal("buildpack")
                .requires(source -> source.getSender().hasPermission("de.aethos.admin"))
                .executes(context -> build(context.getSource().getSender(), manager))
                .build());
    }

    private static int build(final CommandSender sender, final XPManager manager) {
        try {
            final Path pack = manager.buildPack();
            sender.sendMessage(Component.text("Pack gebaut: " + pack + " (" + Files.size(pack) / 1024 + " KB)",
                    NamedTextColor.GREEN));
            return Command.SINGLE_SUCCESS;
        } catch (final IOException | RuntimeException e) {
            sender.sendMessage(Component.text("Pack konnte nicht gebaut werden: " + e.getMessage(), NamedTextColor.RED));
            return 0;
        }
    }
}
