package de.ethos.ethoslib.database;

@FunctionalInterface
public interface Query {

    String createSQL(final String tablePrefix);

}
