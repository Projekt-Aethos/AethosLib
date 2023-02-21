package de.ethos.ethoslib.database;

@FunctionalInterface
public interface Query {
    String createSql(final String tablePrefix);
}
