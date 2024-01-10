package de.aethos.lib;

import de.aethos.lib.data.database.connector.Connector;
import de.aethos.lib.data.database.connector.DefaultPluginConnector;
import de.aethos.lib.inventory.gui.GUIListener;
import de.aethos.lib.inventory.item.ToolListener;
import de.aethos.lib.level.LevelApi;
import de.aethos.lib.util.WorldGuardSupport;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class AethosLib extends JavaPlugin {
    private static AethosLib instance;

    private WorldGuardSupport worldGuardSupport;

    private LevelApi levelApi;

    @Override
    public void onLoad() {
        worldGuardSupport = new WorldGuardSupport(getLogger(), getServer().getPluginManager());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        levelApi = new LevelApi(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GUIListener(), this);
        pm.registerEvents(new ToolListener(), this);

        getLogger().info("✓ AethosLib successfully activated");
    }

    @Contract("_ -> new")
    public @NotNull Connector getConnector(@NotNull JavaPlugin plugin) {
        return new DefaultPluginConnector(plugin);
    }

    public static @NotNull AethosLib getInstance() {
        return instance;
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
}
