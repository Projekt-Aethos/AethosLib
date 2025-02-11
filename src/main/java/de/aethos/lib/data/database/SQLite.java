package de.aethos.lib.data.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public record SQLite(File file) implements Database {
    public SQLite {
        if (!file.toPath().toString().endsWith(".sqlite")) {
            throw new IllegalStateException("File should end with .sqlite");
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        try {
            if (!file.exists()) {
                Files.createDirectory(Path.of(file.getParent()));
                file.createNewFile();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + file.toPath());
    }
}
