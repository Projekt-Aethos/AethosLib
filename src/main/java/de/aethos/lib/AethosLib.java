package de.aethos.lib;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.aethos.lib.compatibility.worldguard.ExistingWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.InactiveWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.WorldGuardSupport;
import de.aethos.lib.data.database.connector.Connector;
import de.aethos.lib.data.database.connector.DefaultPluginConnector;
import de.aethos.lib.level.LevelApi;
import de.aethos.lib.level.LevelPointListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.janboerman.guilib.api.GuiListener;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class AethosLib extends JavaPlugin {
    private static AethosLib instance;

    private WorldGuardSupport worldGuardSupport;

    private LevelApi levelApi;

    public static @NotNull AethosLib getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        loadWorldGuardSupport();
        levelApi = new LevelApi(this);
        getLogger().info("✓ AethosLib successfully loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();


        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(GuiListener.getInstance(), this);
        pm.registerEvents(new LevelPointListener(this), this);

        getLogger().info("✓ AethosLib successfully activated");
    }

    private void loadWorldGuardSupport() {
        Logger logger = getLogger();
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                String version = WorldGuardPlugin.inst().getDescription().getVersion();
                if (version.contains("7.0")) {
                    logger.info("WorldGuard-Unterstützung aktiviert!");
                    worldGuardSupport = new ExistingWorldGuardSupport();
                } else {
                    logger.info("WorldGuard Version 7.0.X erforderlich, vorhanden ist " + version);
                }
            } catch (Exception e) {
                logger.warning("Fehler beim Laden, WorldGuard-Unterstützung deaktiviert!");
            }
        } else {
            logger.info("WorldGuard nicht vorhanden - Unterstützung deaktiviert");
        }
        if (worldGuardSupport == null) {
            worldGuardSupport = new InactiveWorldGuardSupport();
            logger.info("WorldGuardSupport inactive");
        }
    }

    @Contract("_ -> new")
    public @NotNull Connector getConnector(@NotNull JavaPlugin plugin) {
        return new DefaultPluginConnector(plugin);
    }

    public @NotNull WorldGuardSupport getWorldGuardSupport() {
        return worldGuardSupport;
    }

    @Contract(" -> new")
    public @NotNull Connector getConnector() {
        return new DefaultPluginConnector(this);
    }

    public @NotNull LevelApi getLevelApi() {
        return levelApi;
    }


    public boolean isWorldGuardSupportEnabled() {
        return worldGuardSupport.isEnabled();
    }
}
