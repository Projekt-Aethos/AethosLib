package de.ethos.ethoslib.database;

@FunctionalInterface
public interface Update {
    String createSQL(final String tablePrefix);
}
