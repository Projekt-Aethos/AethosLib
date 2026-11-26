package de.aethos.lib;

import com.jeff_media.customblockdata.CustomBlockData;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.aethos.lib.commands.GiveCommand;
import de.aethos.lib.xp.api.PackCommand;
import de.aethos.lib.xp.api.XPCommand;
import de.aethos.lib.compatibility.worldguard.ExistingWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.InactiveWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.WorldGuardSupport;
import de.aethos.lib.data.database.connector.Connector;
import de.aethos.lib.data.database.connector.DefaultPluginConnector;
import de.aethos.lib.items.DefaultItemFlagListener;
import de.aethos.lib.level.LevelApi;
import de.aethos.lib.level.LevelPointListener;
import de.aethos.lib.web.WebServerManager;
import de.aethos.lib.xp.api.XPManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import xyz.janboerman.guilib.api.GuiListener;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class AethosLib extends JavaPlugin {

    private final LevelApi levelApi;

    private final WorldGuardSupport worldGuardSupport;

    private final WebServerManager webServerManager;

    private final XPManager xpmanager;

    private ItemRegistry itemRegistry;

    public AethosLib() {
        worldGuardSupport = loadWorldGuardSupport();
        levelApi = new LevelApi(this);
        xpmanager = new XPManager(this);
        webServerManager = new WebServerManager();
    }

    public XPManager getXPmanager() {
        return xpmanager;
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

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(GuiListener.getInstance(), this);
        pm.registerEvents(new LevelPointListener(this), this);
        pm.registerEvents(new DefaultItemFlagListener(), this);

        CustomBlockData.registerListener(this);

        itemRegistry = new ItemRegistry(this, getLogger());
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            GiveCommand.register(event.registrar().getDispatcher(), itemRegistry);
            XPCommand.register(event.registrar(), xpmanager);
            PackCommand.register(event.registrar(), xpmanager);
        });

        xpmanager.load(getConfig());
        xpmanager.registerListeners();

        getLogger().info("✓ AethosLib successfully activated");
    }

    @Override
    public void onDisable() {
        xpmanager.shutdown();
    }

    private WorldGuardSupport loadWorldGuardSupport() {
        final Logger logger = getLogger();
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                final String version = WorldGuardPlugin.inst().getPluginMeta().getVersion();
                if (version.contains("7.0")) {
                    return new ExistingWorldGuardSupport(getLogger());
                }
                logger.info("WorldGuard Version 7.0.X erforderlich, vorhanden ist " + version);
            } catch (final Exception e) {
                logger.log(Level.WARNING, "Fehler beim Laden, WorldGuard-Unterstützung deaktiviert!", e);
            }
        }
        return new InactiveWorldGuardSupport(this);
    }

    @Contract("_ -> new")
    public Connector getConnector(final JavaPlugin plugin) {
        return new DefaultPluginConnector(plugin);
    }

    public WorldGuardSupport getWorldGuardSupport() {
        return worldGuardSupport;
    }

    @Contract(" -> new")
    public Connector getConnector() {
        return new DefaultPluginConnector(this);
    }

    @Deprecated
    public LevelApi getLevelApi() {
        return levelApi;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public WebServerManager getWebServerManager() {
        return webServerManager;
    }

    public boolean isWorldGuardSupportEnabled() {
        return worldGuardSupport.isEnabled();
    }
}
