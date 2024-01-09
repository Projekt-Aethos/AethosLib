package de.aethos.lib.data.database.action;


import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface Update {
    void update(@NotNull Connection connection) throws SQLException;
}
