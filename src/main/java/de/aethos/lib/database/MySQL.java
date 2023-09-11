package de.aethos.lib.database;

import de.aethos.lib.util.Helper;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class MySQL extends Database {
    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;

    public MySQL(JavaPlugin plugin, String hostname, String port, String database, String username, String password) {
        super(plugin);
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    @Override
    public Connection openConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database + "?&useSSL=false",
                    this.user, this.password);
        } catch (ClassNotFoundException | SQLException e) {
            Helper.log(Level.WARNING, "MySQL says: " + e.getMessage());
        }
        return connection;
    }

}
