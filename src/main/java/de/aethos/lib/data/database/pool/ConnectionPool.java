package de.aethos.lib.data.database.pool;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface ConnectionPool extends Supplier<Connection> {
    @Deprecated
    default void give(Connection connection){
        releaseConnection(connection);
    }
    @Deprecated
    default Connection get(){
        return acquireConnection().join();
    }

    int size();

    void releaseConnection(Connection connection);
    CompletableFuture<Connection> acquireConnection();
}
