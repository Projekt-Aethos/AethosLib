package de.aethos.lib.data.database;


import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public sealed interface Database permits MySQL, SQLite {

    @NotNull Connection createConnection();

}
