package de.aethos.lib.data.database.pool;

import de.aethos.lib.AethosLib;
import de.aethos.lib.data.database.Database;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ConnectionPoolImpl implements Closeable, ConnectionPool {

    private final BlockingQueue<Connection> queue;
    private final Database database;
    private final ExecutorService service = Executors.newCachedThreadPool();
    private final Logger logger = AethosLib.getPlugin(AethosLib.class).getLogger();

    protected ConnectionPoolImpl(Database database, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        queue = new LinkedBlockingQueue<>(size);
        this.database = database;
        for (int i = 0; i < size; i++) {
            service.submit(() -> {
                try {
                    final Connection connection = database.createConnection();
                    queue.put(connection);
                    logger.info("Connection created " + connection);
                } catch (InterruptedException | SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }

    }

    public static ConnectionPool newSingleConnectionPool(Database database) {
        return new ConnectionPoolImpl(database, 1);
    }

    public static ConnectionPool newConnectionPool(Database database, int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        return new ConnectionPoolImpl(database, size);
    }


    public CompletableFuture<Connection> acquireConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Thread was interrupted while waiting for a connection", e);
            }
        }, service);
    }

    public void releaseConnection(Connection connection) {
        service.submit(() -> {
            try {
                if (!connection.isClosed()) {
                    if (!queue.offer(connection, 1, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Pool is full. Couldn't offer connection.");
                    }
                } else {
                    queue.offer(database.createConnection());
                }
            } catch (SQLException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Override
    public void close() throws IOException {
        service.shutdown();
        try {
            for (Connection connection : queue) {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            }
        } catch (SQLException e) {
            throw new IOException("Error shutting down the connection pool", e);
        }
    }



    @Override
    public int size() {
        return queue.size();
    }
}
