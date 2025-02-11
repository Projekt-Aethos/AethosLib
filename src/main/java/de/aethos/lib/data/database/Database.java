package de.aethos.lib.data.database;

import java.sql.Connection;
import java.sql.SQLException;

public sealed interface Database permits MySQL, SQLite {
    Connection createConnection() throws SQLException;
}
