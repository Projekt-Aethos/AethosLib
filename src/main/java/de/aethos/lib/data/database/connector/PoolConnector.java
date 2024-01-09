package de.aethos.lib.data.database.connector;

import de.aethos.lib.data.database.action.Query;
import de.aethos.lib.data.database.action.Update;
import de.aethos.lib.data.database.pool.ConnectionPool;
import de.aethos.lib.result.Result;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;

public class PoolConnector implements Connector {
    private final ConnectionPool pool;

    public PoolConnector(@NotNull ConnectionPool pool) {
        this.pool = pool;
    }

    public <T> @NotNull Result<T, SQLException> query(@NotNull Query<T> query) {
        final Connection connection = pool.get();
        final Result<T, SQLException> value = query(query, connection);
        pool.give(connection);
        return value;
    }

    private <T> @NotNull Result<T, SQLException> query(@NotNull Query<T> query, @NotNull Connection connection) {
        try {
            try {
                final T t = query.query(connection);
                return Result.ok(t);
            } catch (SQLException sql) {
                return Result.err(sql);
            }
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException sql) {
                return Result.err(sql);
            }
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull Stream<Result<T, SQLException>> query(Query<T>... queries) {
        if (queries == null) {
            return Stream.empty();
        }
        final Result<T, SQLException>[] results = new Result[queries.length];
        for (int i = 0; i < queries.length; i++) {
            final Connection connection = pool.get();
            results[i] = query(queries[i], connection);
            pool.give(connection);
        }
        return Arrays.stream(results);
    }

    public void update(@NotNull Update update) {
        final Connection connection = pool.get();
        update(update, connection);
    }

    public void update(Update... update) {
        for (Update value : update) {
            final Connection connection = pool.get();
            update(value, connection);
            pool.give(connection);
        }
    }

    private void update(Update update, @NotNull Connection connection) {
        try {
            update.update(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
