package de.aethos.lib.util;

import de.aethos.lib.AethosLib;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Helper {
    private static final Logger LOGGER = AethosLib.getInstance().getLogger();

    private Helper() {
    }

    public static void log(@NotNull String text) {
        log(Level.INFO, text);
    }

    public static void log(@NotNull Level level, @NotNull String text) {
        LOGGER.log(level, text);
    }

    public static void log(@NotNull Level level, @NotNull String text, @NotNull Throwable thrown) {
        LOGGER.log(level, text, thrown);
    }

    public static void logDebug(@NotNull String text) {
        LOGGER.log(AethosLib.isDebugEnabled ? Level.INFO : Level.CONFIG, "[Debug] " + text);
    }
}