package de.aethos.lib.data.database.pool;

import de.aethos.lib.data.database.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class QueuedConnectionPool implements ConnectionPool {
    private final Queue<Future<Connection>> queue = new LinkedList<>();

    private final Database database;

    private final ExecutorService executorService;

    private final int min;

    private final Logger logger;

    public QueuedConnectionPool(final Database database, final int min, final Logger logger) {
        this.database = database;
        this.logger = logger;
        this.executorService = Executors.newSingleThreadExecutor();
        this.min = min;
        for (int i = 0; i < min; i++) {
            queue.add(executorService.submit(database::createConnection));
        }
    }

    public void give(final Connection connection) {
        try {
            if (!connection.isClosed()) {
                queue.add(executorService.submit(() -> connection));
            }
        } catch (final SQLException ignore) {

        }
    }

    public Connection get() {
        Future<Connection> future = queue.poll();
        while (true) {
            if (future == null) {
                for (int j = 0; j < min; j++) {
                    queue.add(executorService.submit(database::createConnection));
                }
                future = queue.poll();
                continue;
            }
            if (future.isCancelled()) {
                future = queue.poll();
                continue;
            }
            if (!future.isDone()) {
                queue.add(future);
                future = queue.poll();
                continue;
            }
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException exception) {
                logger.warning(exception.getMessage());
                future = queue.poll();
            }
        }
    }

    @Override
    public int size() {
        return queue.size();
    }
}
