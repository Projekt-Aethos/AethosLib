package de.aethos.lib.database;

import de.aethos.lib.util.Helper;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public abstract class Database {

    protected final Plugin plugin;
    protected final String prefix;
    protected Connection con;

    protected Database(JavaPlugin plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getConfig().getString("mysql.prefix", "");
    }

    public Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                con = openConnection();
            }
        } catch (SQLException e) {
            Helper.log(Level.WARNING, "Failed opening database connection: " + e.getMessage(), e);
        }
        return con;
    }

    protected abstract Connection openConnection() throws SQLException;

    public void closeConnection() {
        try {
            con.close();
        } catch (SQLException e) {
            Helper.log(Level.SEVERE, "There was an exception with SQL", e);
        }
        con = null;
    }

}
