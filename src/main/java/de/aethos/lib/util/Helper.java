package de.aethos.lib.util;

import de.aethos.lib.AethosLib;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Helper {
    private static final Logger LOGGER = AethosLib.getInstance().getLogger();

    private Helper() {
    }

    public static void log(String text) {
        log(Level.INFO, text);
    }

    public static void log(Level level, String text) {
        LOGGER.log(level, text);
    }

    public static void log(Level level, String text, Throwable thrown) {
        LOGGER.log(level, text, thrown);
    }

    public static void logDebug(String text) {
        LOGGER.log(AethosLib.isDebugEnabled ? Level.INFO : Level.CONFIG, "[Debug] " + text);
    }

    private static void sendComponent(@Nullable Component prefix, @NotNull Player p, @NotNull Component message) {
        p.sendMessage(Component.text().append(Component.text(AethosLib.chatPrefix)).append(message));
    }

}