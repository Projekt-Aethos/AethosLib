package de.aethos.lib;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.aethos.lib.compatibility.worldguard.ExistingWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.InactiveWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.WorldGuardSupport;
import de.aethos.lib.data.database.connector.Connector;
import de.aethos.lib.data.database.connector.DefaultPluginConnector;
import de.aethos.lib.items.DefaultItemFlagListener;
import de.aethos.lib.level.LevelApi;
import de.aethos.lib.level.LevelPointListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import xyz.janboerman.guilib.api.GuiListener;

import java.util.Locale;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class AethosLib extends JavaPlugin {
    private final LevelApi levelApi;

    private final WorldGuardSupport worldGuardSupport;

    public AethosLib() {
        worldGuardSupport = loadWorldGuardSupport();
        levelApi = new LevelApi(this);
    }

    /**
     * Gets a NamespacedKey within the plugin's namespace.
     *
     * @param value the {@link NamespacedKey#isValidKey(String) valid} key value, except upper cases are allowed
     * @return a new NamespacedKey
     */
    public static NamespacedKey getKey(final String value) {
        return new NamespacedKey("AethosLib".toLowerCase(Locale.ROOT), value.toLowerCase(Locale.ROOT));
    }

    @Override
    public void onLoad() {
        getLogger().info("✓ AethosLib successfully loaded");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(GuiListener.getInstance(), this);
        pm.registerEvents(new LevelPointListener(this), this);
        pm.registerEvents(new DefaultItemFlagListener(), this);

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
    public Connector getConnector(JavaPlugin plugin) {
        return new DefaultPluginConnector(plugin);
    }

    public WorldGuardSupport getWorldGuardSupport() {
        return worldGuardSupport;
    }

    @Contract(" -> new")
    public Connector getConnector() {
        return new DefaultPluginConnector(this);
    }

    public LevelApi getLevelApi() {
        return levelApi;
    }

    public boolean isWorldGuardSupportEnabled() {
        return worldGuardSupport.isEnabled();
    }
}
