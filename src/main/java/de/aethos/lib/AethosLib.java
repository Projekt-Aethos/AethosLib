package de.aethos.lib;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.aethos.lib.blocks.BlockListener;
import de.aethos.lib.blocks.BlockType;
import de.aethos.lib.blocks.example.AnotherFurniture;
import de.aethos.lib.blocks.example.Furniture;
import de.aethos.lib.commands.FurnitureCommand;
import de.aethos.lib.compatibility.worldguard.ExistingWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.InactiveWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.WorldGuardSupport;
import de.aethos.lib.data.database.connector.Connector;
import de.aethos.lib.data.database.connector.DefaultPluginConnector;
import de.aethos.lib.level.LevelApi;
import de.aethos.lib.level.LevelPointListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.janboerman.guilib.api.GuiListener;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class AethosLib extends JavaPlugin {
    private final LevelApi levelApi;
    private final WorldGuardSupport worldGuardSupport;

    public AethosLib() {
        worldGuardSupport = loadWorldGuardSupport();
        levelApi = new LevelApi(this);
    }

    public static NamespacedKey getKey(String key) {
        return new NamespacedKey(AethosLib.getPlugin(AethosLib.class), key);
    }

    @Override
    public void onLoad() {
        getLogger().info("✓ AethosLib successfully loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void onEnable() {
        try {
            BlockType.Register.register(AnotherFurniture.class);
            BlockType.Register.register(Furniture.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        saveDefaultConfig();
        getServer().getCommandMap().register("aehtoslib", new FurnitureCommand("furniture"));
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(BlockListener.INSTANCE, this);
        pm.registerEvents(GuiListener.getInstance(), this);
        pm.registerEvents(new LevelPointListener(this), this);

        getLogger().info("✓ AethosLib successfully activated");
    }

    private WorldGuardSupport loadWorldGuardSupport() {
        Logger logger = getLogger();
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                String version = WorldGuardPlugin.inst().getDescription().getVersion();
                if (version.contains("7.0")) {
                    return new ExistingWorldGuardSupport(this);
                } else {
                    logger.info("WorldGuard Version 7.0.X erforderlich, vorhanden ist " + version);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Fehler beim Laden, WorldGuard-Unterstützung deaktiviert!");
            }
        }
        return new InactiveWorldGuardSupport(this);
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
