package de.aethos.lib.data.database.pool;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.function.Supplier;

public interface ConnectionPool extends Supplier<Connection> {
    void give(@NotNull Connection connection);

    @NotNull Connection get();

    int size();
}
