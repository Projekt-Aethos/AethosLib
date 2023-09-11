package de.aethos.lib.database;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Update {
    String createSQL(@NotNull String tablePrefix);
}
