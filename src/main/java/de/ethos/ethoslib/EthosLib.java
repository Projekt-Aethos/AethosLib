package de.ethos.ethoslib;

import de.ethos.ethoslib.inventory.gui.GUIListener;
import de.ethos.ethoslib.inventory.item.ToolListener;
import de.ethos.ethoslib.database.*;
import de.ethos.ethoslib.util.Helper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class EthosLib extends JavaPlugin {
    private static EthosLib INSTANCE;
    public static String chatPrefix;
    public static boolean isDebugEnabled;
    private Database database;
    private boolean isMySQLUsed;

    @Override
    public void onEnable() {
        INSTANCE = this;
        // Plugin startup logic
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        final boolean mySQLEnabled = config.getBoolean("mysql.enabled", true);
        if (mySQLEnabled) {
            Helper.logDebug("Connecting to MySQL database");
            this.database = new MySQL(this,
                    config.getString("mysql.host"),
                    config.getString("mysql.port"),
                    config.getString("mysql.base"),
                    config.getString("mysql.user"),
                    config.getString("mysql.pass"));
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


        getServer().getPluginManager().registerEvents(new GUIListener(),this);
        getServer().getPluginManager().registerEvents(new ToolListener(),this);
        Helper.log("✓ EthosSkills successfully activated");
        
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



    //////////          Getter          ////////////
    public static EthosLib getINSTANCE() {
        return INSTANCE;
    }

    @Contract("_ -> new")
    public @NotNull Connector getConnector(@NotNull JavaPlugin plugin) {
        return new Connector(plugin, database);
    }

    public @NotNull Database getDatabase(){
        return database;
    }

    public static void error(Exception e){
        e.printStackTrace(System.out);
    }


}
