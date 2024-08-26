package de.aethos.lib.data.database.pool;

import de.aethos.lib.data.database.Database;

import java.sql.Connection;
import java.sql.SQLException;

public class FixedSizedConnectionPool implements ConnectionPool {
    private final Connection[] pool;

    private final Database database;

    private int pointer = 0;

    public FixedSizedConnectionPool(final Database database, final int size) {
        this.database = database;
        this.pool = new Connection[size];
    }

    @Override
    public void give(final Connection connection) {
        try {
            connection.close();
        } catch (final SQLException ignore) {

        }
    }

    @Override
    public Connection get() {
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
