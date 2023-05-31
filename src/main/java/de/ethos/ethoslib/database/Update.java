package de.ethos.ethoslib.database;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Update {
    String createSQL(@NotNull String tablePrefix);
}
