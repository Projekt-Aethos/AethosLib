package de.ethos.ethoslib.database.future;


import org.jetbrains.annotations.Contract;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * FunctionalInterface to build a Class from a ResultSet
 */
@FunctionalInterface
public interface Builder<T> {
    @Contract("_ -> new")
    T build(ResultSet resultSet) throws SQLException;

}
