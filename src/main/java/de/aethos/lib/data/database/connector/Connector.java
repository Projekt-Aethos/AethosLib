package de.aethos.lib.data.database.connector;

import de.aethos.lib.data.database.action.Query;
import de.aethos.lib.data.database.action.Update;
import de.aethos.lib.result.Result;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.stream.Stream;

public interface Connector {
    <T> @NotNull Result<T, SQLException> query(@NotNull Query<T> query);

    <T> @NotNull Stream<Result<T, SQLException>> query(Query<T>... queries);

    void update(Update... update);

    void update(@NotNull Update update);
}
