package de.ethos.ethoslib.database;

import de.ethos.ethoslib.util.Helper;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
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

    @Deprecated
    public void write(@NotNull Update type) throws SQLException {
        String sql = type.createSQL(prefix);
        Helper.logDebug(sql);
        refresh();
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    @Deprecated
    public ResultSet search(@NotNull Query type) throws SQLException {
        String sql = type.createSQL(prefix);
        refresh();
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
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
        Helper.logDebug(sql);
        refresh();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
        return preparedStatement;
    }
}
