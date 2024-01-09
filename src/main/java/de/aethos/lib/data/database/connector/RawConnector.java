package de.aethos.lib.data.database.connector;

import de.aethos.lib.data.database.Database;
import de.aethos.lib.data.database.action.Query;
import de.aethos.lib.data.database.action.Update;
import de.aethos.lib.result.Result;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Stream;


public class RawConnector implements Connector {

    private final Database database;

    public RawConnector(@NotNull Database database) {
        this.database = database;

    }

    public <T> @NotNull Result<T, SQLException> query(@NotNull Query<T> query) {
        try (Connection connection = database.createConnection()) {
            return query(query, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        try (Connection connection = database.createConnection()) {
            for (int i = 0; i < queries.length; i++) {
                results[i] = query(queries[i], connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Arrays.stream(results);
    }

    @Override
    public void update(Update... updates) {
        try (Connection connection = database.createConnection()) {
            for (Update update : updates) {
                update(update, connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void update(@NotNull Update update) {
        try (Connection connection = database.createConnection()) {
            update(update, connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void update(Update update, @NotNull Connection connection) {
        try {
            update.update(connection);
        } catch (SQLException ignore) {

        }
    }


}
