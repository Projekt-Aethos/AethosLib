package de.ethos.ethoslib;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.ethos.ethoslib.database.Connector;
import de.ethos.ethoslib.database.Database;
import de.ethos.ethoslib.database.MySQL;
import de.ethos.ethoslib.database.SQLite;
import de.ethos.ethoslib.inventory.gui.GUIListener;
import de.ethos.ethoslib.inventory.item.ToolListener;
import de.ethos.ethoslib.util.Helper;
import de.ethos.ethoslib.util.WorldGuardSupport;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class EthosLib extends JavaPlugin {
    public static String chatPrefix;
    public static boolean isDebugEnabled;
    public static boolean isWorldGuardEnabled;
    private static EthosLib INSTANCE;
    private static WorldGuardSupport worldGuardSupport;
    private Database database;
    private boolean isMySQLUsed;

    //////////          Getter          ////////////
    public static EthosLib getINSTANCE() {
        return INSTANCE;
    }

    public static void error(@NotNull Exception exception) {
        exception.printStackTrace(System.out);
    }

    public static WorldGuardSupport getWorldGuardSupport() {
        return worldGuardSupport;
    }

    @Override
    public void onLoad() {
        //erlaube registrieren der WorldGuard-Flags, falls WorldGuard 7.0 vorhanden ist
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null && WorldGuardPlugin.inst().getDescription().getVersion().contains("7.0")) {
            isWorldGuardEnabled = true;
        }
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        // Plugin startup logic
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        final boolean mySQLEnabled = config.getBoolean("mysql.enabled", true);
        if (mySQLEnabled) {
            Helper.logDebug("Connecting to MySQL database");
            this.database = new MySQL(this, config.getString("mysql.host"), config.getString("mysql.port"), config.getString("mysql.base"), config.getString("mysql.user"), config.getString("mysql.pass"));
            if (database.getConnection() != null) {
                isMySQLUsed = true;
                Helper.log(Level.INFO, "Successfully connected to MySQL database!");
            }
        }
        if (!mySQLEnabled || !isMySQLUsed) {
            this.database = new SQLite(this, "database.db");
            if (mySQLEnabled) {
                Helper.log(Level.WARNING, "No connection to the mySQL Database! Using SQLite for storing data as fallback!");
            } else {
                Helper.log(Level.INFO, "Using SQLite for storing data!");
            }
        }
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GUIListener(), this);
        pm.registerEvents(new ToolListener(), this);

        //WorldGuard-Aktivierung
        Bukkit.getScheduler().runTask(this, () -> {
            isWorldGuardEnabled = pm.isPluginEnabled("WorldGuard");
            if (isWorldGuardEnabled) {
                if (WorldGuardPlugin.inst().getDescription().getVersion().contains("7.0")) {
                    worldGuardSupport = new WorldGuardSupport();
                    worldGuardSupport.loadRegions();
                } else {
                    isWorldGuardEnabled = false;
                    Helper.log("WorldGuard Version 7.0.X erforderlich, vorhanden ist " + WorldGuardPlugin.inst().getDescription().getVersion());
                }
            } else {
                Helper.log("WorldGuard-Unterstützung deaktiviert");
            }
        });

        Helper.log("✓ EthosLib successfully activated");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        isDebugEnabled = getConfig().getBoolean("debug", false);

        chatPrefix = getConfig().getString("chatPrefix", null);
    }

    @Contract("_ -> new")
    public @NotNull Connector getConnector(@NotNull JavaPlugin plugin) {
        return new Connector(plugin, database);
    }

    public @NotNull Database getDatabase() {
        return database;
    }

}
