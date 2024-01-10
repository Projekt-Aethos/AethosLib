package de.aethos.lib;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.aethos.lib.data.database.connector.Connector;
import de.aethos.lib.data.database.connector.DefaultPluginConnector;
import de.aethos.lib.inventory.gui.GUIListener;
import de.aethos.lib.inventory.item.ToolListener;
import de.aethos.lib.level.LevelApi;
import de.aethos.lib.util.WorldGuardSupport;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class AethosLib extends JavaPlugin {
    public static String chatPrefix;

    public static boolean isDebugEnabled;

    public static boolean isWorldGuardEnabled;

    private static AethosLib instance;

    private static WorldGuardSupport worldGuardSupport;

    private LevelApi levelApi;

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        isDebugEnabled = getConfig().getBoolean("debug", false);

        chatPrefix = getConfig().getString("chatPrefix", "[ÆL] ");
    }

    @Override
    public void onLoad() {
        //erlaube registrieren der WorldGuard-Flags, falls WorldGuard 7.0 vorhanden ist
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null
                && WorldGuardPlugin.inst().getDescription().getVersion().contains("7.0")) {
            isWorldGuardEnabled = true;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void onEnable() {
        instance = this;

        final DefaultPluginConnector connector = new DefaultPluginConnector(this);

        // Plugin startup logic
        saveDefaultConfig();

        levelApi = new LevelApi(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GUIListener(), this);
        pm.registerEvents(new ToolListener(), this);

        Logger logger = getLogger();
        //WorldGuard-Aktivierung
        Bukkit.getScheduler().runTask(this, () -> {
            isWorldGuardEnabled = pm.isPluginEnabled("WorldGuard");
            if (isWorldGuardEnabled) {
                String version = WorldGuardPlugin.inst().getDescription().getVersion();
                if (version.contains("7.0")) {
                    worldGuardSupport = new WorldGuardSupport();
                    isWorldGuardEnabled = worldGuardSupport.loadRegions();
                    if (isWorldGuardEnabled) {
                        logger.log(Level.INFO, "WorldGuard-Unterstützung aktiviert!");
                    } else {
                        logger.log(Level.WARNING, "Fehler beim Laden, WorldGuard-Unterstützung deaktiviert!");
                    }
                } else {
                    isWorldGuardEnabled = false;
                    logger.info("WorldGuard Version 7.0.X erforderlich, vorhanden ist " + version);
                }
            } else {
                logger.info("WorldGuard-Unterstützung deaktiviert");
            }
        });

        logger.info("✓ AethosLib successfully activated");
    }

    @Contract("_ -> new")
    public @NotNull Connector getConnector(@NotNull JavaPlugin plugin) {
        return new DefaultPluginConnector(plugin);
    }

    public static AethosLib getInstance() {
        return instance;
    }

    public static WorldGuardSupport getWorldGuardSupport() {
        return worldGuardSupport;
    }

    @Contract(" -> new")
    public @NotNull Connector getConnector() {
        return new DefaultPluginConnector(this);
    }

    public @NotNull LevelApi getLevelApi() {
        return levelApi;
    }
}
