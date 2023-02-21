package de.ethos.ethoslib.database;

import de.ethos.ethoslib.util.Helper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class SQLite extends Database {
    private final String dbLocation;

    public SQLite(final JavaPlugin plugin, final String dbLocation) {
        super(plugin);
        this.dbLocation = dbLocation;
    }

    @Override
    public Connection openConnection() {
        final File rootFile = new File(plugin.getDataFolder(), "databases");
        rootFile.mkdirs();
        final File file = new File(rootFile, dbLocation);
        if (!(file.exists())) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
                Helper.log(Level.INFO, "Unable to create database!");
            }
        }
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager
                    .getConnection("jdbc:sqlite:" + rootFile.toPath() + "/" + dbLocation);
        } catch (ClassNotFoundException | SQLException e) {
            Helper.log(Level.INFO, "There was an exception with SQL");
        }
        return connection;
    }
}
