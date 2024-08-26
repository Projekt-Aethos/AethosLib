package de.aethos.lib.data.database.action;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface Query<T> {
    T query(Connection connection) throws SQLException;
}
