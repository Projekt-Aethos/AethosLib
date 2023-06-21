package de.ethos.ethoslib.database;

import de.ethos.ethoslib.util.Helper;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class Connector {
    private final String prefix;
    private final Database database;
    private Connection connection;

    public Connector(final @NotNull JavaPlugin plugin, final @NotNull Database database) {
        this.database = database;
        this.prefix = plugin.getName() + "_";
        connection = database.getConnection();
        refresh();
    }

    /**
     * This method should be used before any other database operations.
     */
    public final void refresh() {
        try {
            connection.prepareStatement("SELECT 1").executeQuery().close();
        } catch (final SQLException e) {
            Helper.log(Level.WARNING, "Reconnecting to the database");
            closeConnection();
            connection = database.getConnection();
        }
    }

    private void closeConnection() {
        try {
            connection.close();
        } catch (final SQLException e) {
            Helper.log(Level.SEVERE, "There was an exception with SQL", e);
        }
        connection = null;
    }

    //Nach EthosSkills-Syntax
    public void write(final @NotNull Update type) throws SQLException {
        final String sql = type.createSQL(prefix);
        Helper.logDebug(sql);
        connection = database.getConnection();
        final Statement stmt = database.con.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    //Nach EthosSkills-Syntax
    public ResultSet search(final @NotNull Query type) throws SQLException {
        final String sql = type.createSQL(prefix);
        Helper.logDebug(sql);
        connection = database.getConnection();
        final Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
}
