package de.aethos.lib.data.database.connector;

import de.aethos.lib.data.database.action.Query;
import de.aethos.lib.data.database.action.Update;
import de.aethos.lib.data.database.pool.ConnectionPool;
import de.aethos.lib.result.Result;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;

public class PoolConnector implements Connector {
    private final ConnectionPool pool;

    public PoolConnector(final ConnectionPool pool) {
        this.pool = pool;
    }

    @Override
    public <T> Result<T, SQLException> query(final Query<T> query) {
        final Connection connection = pool.get();
        final Result<T, SQLException> value = query(query, connection);
        pool.give(connection);
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Stream<Result<T, SQLException>> query(final Query<T>... queries) {
        if (queries.length == 0) {
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

    @Override
    public void update(final Update... update) {
        for (final Update value : update) {
            final Connection connection = pool.get();
            update(value, connection);
            pool.give(connection);
        }
    }

    @Override
    public void update(final Update update) {
        final Connection connection = pool.get();
        update(update, connection);
    }

    private <T> Result<T, SQLException> query(final Query<T> query, final Connection connection) {
        try {
            final T t = query.query(connection);
            return Result.ok(t);
        } catch (final SQLException sql) {
            return Result.err(sql);
        } catch (final RuntimeException e) {
            if (e.getCause() instanceof SQLException sql) {
                return Result.err(sql);
            }
            return Result.err(new SQLException(e));
        }
    }

    private void update(final Update update, final Connection connection) {
        try {
            update.update(connection);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
