package de.aethos.lib.data.database;

import java.sql.Connection;

public sealed interface Database permits MySQL, SQLite {
    Connection createConnection();
}
