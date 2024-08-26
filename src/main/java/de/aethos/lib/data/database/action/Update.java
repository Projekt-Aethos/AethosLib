package de.aethos.lib.data.database.action;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface Update {
    void update(Connection connection) throws SQLException;
}
