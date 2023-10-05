package de.aethos.lib.database;

import de.aethos.lib.util.Helper;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class Connector {
    private final String prefix;
    private final Database database;
    private Connection connection;

    public Connector(@NotNull JavaPlugin plugin, @NotNull Database database) {
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
        } catch (SQLException e) {
            Helper.log(Level.WARNING, "Reconnecting to the database");
            closeConnection();
            connection = database.getConnection();
        }
    }

    private void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            Helper.log(Level.SEVERE, "There was an exception with SQL", e);
        }
        connection = null;
    }

    public void write(@NotNull Update type, Object @NotNull ... values) throws SQLException {
        PreparedStatement stmt = fill(type.createSQL(prefix), values);
        stmt.executeUpdate();
        stmt.close();
    }

    public ResultSet search(@NotNull Query type, Object @NotNull ... values) throws SQLException {
        return fill(type.createSQL(prefix), values).executeQuery();
    }

    private PreparedStatement fill(@NotNull String sql, @NotNull Object @NotNull ... values) throws SQLException {
        refresh();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
        Helper.logDebug(preparedStatement.toString());
        return preparedStatement;
    }

    public Connection getConnection() {
        refresh();
        return connection;
    }
}
