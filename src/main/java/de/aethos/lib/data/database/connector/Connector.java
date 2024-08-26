package de.aethos.lib.data.database.connector;

import de.aethos.lib.data.database.action.Query;
import de.aethos.lib.data.database.action.Update;
import de.aethos.lib.result.Result;

import java.sql.SQLException;
import java.util.stream.Stream;

public interface Connector {
    <T> Result<T, SQLException> query(Query<T> query);

    <T> Stream<Result<T, SQLException>> query(Query<T>... queries);

    void update(Update... update);

    void update(Update update);
}
