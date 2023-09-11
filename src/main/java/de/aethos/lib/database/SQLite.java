package de.aethos.lib.database;

import de.aethos.lib.util.Helper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class SQLite extends Database {
    private final String dbLocation;

    public SQLite(JavaPlugin plugin, String dbLocation) {
        super(plugin);
        this.dbLocation = dbLocation;
    }

    @Override
    public Connection openConnection() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File file = new File(plugin.getDataFolder(), dbLocation);
        if (!(file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Helper.log(Level.SEVERE, "Unable to create database!", e);
            }
        }
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().toPath() + "/" + dbLocation);
        } catch (ClassNotFoundException | SQLException e) {
            Helper.log(Level.SEVERE, "There was an exception with SQL", e);
        }
        return connection;
    }
}
