package de.aethos.lib.data.database.pool;

import java.sql.Connection;
import java.util.function.Supplier;

public interface ConnectionPool extends Supplier<Connection> {
    void give(Connection connection);

    Connection get();

    int size();
}
