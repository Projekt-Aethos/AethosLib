package de.aethos.lib.data.database.pool;

import de.aethos.lib.data.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class FixedSizedConnectionPool implements ConnectionPool {
    private final Connection[] pool;
    private final Database database;
    private int pointer = 0;

    public FixedSizedConnectionPool(@NotNull Database database, int size) {
        this.database = database;
        this.pool = new Connection[size];
    }

    @Override
    public void give(@NotNull Connection connection) {
        try {
            connection.close();
        } catch (SQLException ignore) {
            
        }
    }

    @Override
    public @NotNull Connection get() {
        final Connection con = pool[pointer];
        pool[pointer] = database.createConnection();
        pointer = (pointer + 1) % pool.length;
        return con;
    }

    @Override
    public int size() {
        return pool.length;
    }


}
