package de.ethos.ethoslib.database;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Query {

    String createSQL(@NotNull String tablePrefix);

}
