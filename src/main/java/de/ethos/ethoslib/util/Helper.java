package de.ethos.ethoslib.util;

import de.ethos.ethoslib.EthosLib;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Helper {
    private static final Logger LOGGER = EthosLib.getINSTANCE().getLogger();
    private static final String chatPrefix = EthosLib.chatPrefix;

    public static void log(String text){
        log(Level.INFO, text);
    }

    public static void log(Level level, String text){
        LOGGER.log(level, text);
    }

    public static void logDebug(String text){
        LOGGER.log(EthosLib.isDebugEnabled ? Level.INFO : Level.CONFIG, "[Debug] " + text);
    }

    private static void sendComponent(@Nullable Component prefix, @NotNull Player p, @NotNull Component message){
        p.sendMessage(Component.text().append(Component.text(chatPrefix != null ? chatPrefix : "[EL] ")).append(message));
    }

}