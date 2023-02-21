package de.ethos.ethoslib.database;

@FunctionalInterface
public interface Update {
    String createSql(final String tablePrefix);
}
