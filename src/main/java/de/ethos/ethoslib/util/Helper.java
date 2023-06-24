package de.ethos.ethoslib.util;

import de.ethos.ethoslib.EthosLib;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Helper {
    private static final Logger LOGGER = EthosLib.getINSTANCE().getLogger();

    private static final String CHAT_PREFIX = EthosLib.chatPrefix;

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
        LOGGER.log(EthosLib.isDebugEnabled ? Level.INFO : Level.CONFIG, "[Debug] " + text);
    }

    private static void sendComponent(@Nullable Component prefix, @NotNull Player p, @NotNull Component message) {
        p.sendMessage(Component.text().append(Component.text(CHAT_PREFIX != null ? CHAT_PREFIX : "[EL] ")).append(message));
    }

}