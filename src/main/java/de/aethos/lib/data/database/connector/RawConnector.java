package de.aethos.lib.data.database.connector;

import de.aethos.lib.data.database.Database;
import de.aethos.lib.data.database.action.Query;
import de.aethos.lib.data.database.action.Update;
import de.aethos.lib.result.Result;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;

public class RawConnector implements Connector {
    private final Database database;

    public RawConnector(final Database database) {
        this.database = database;
    }

    @Override
    public <T> Result<T, SQLException> query(final Query<T> query) {
        try (Connection connection = database.createConnection()) {
            return query(query, connection);
        } catch (final SQLException e) {
            return Result.err(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Stream<Result<T, SQLException>> query(final Query<T>... queries) {
        if (queries.length == 0) {
            return Stream.empty();
        }
        final Result<T, SQLException>[] results = new Result[queries.length];
        try (Connection connection = database.createConnection()) {
            for (int i = 0; i < queries.length; i++) {
                results[i] = query(queries[i], connection);
            }
        } catch (final SQLException e) {
            return Stream.of(Result.err(e));
        }
        return Arrays.stream(results);
    }

    @Override
    public void update(final Update... updates) {
        try (Connection connection = database.createConnection()) {
            for (final Update update : updates) {
                update.update(connection);
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(final Update update) {
        try (Connection connection = database.createConnection()) {
            update.update(connection);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
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
}
