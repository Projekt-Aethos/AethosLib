package de.aethos.lib.data.database;

import org.jetbrains.annotations.NotNull;

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
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        try {
            if (!file.exists()) {
                Files.createDirectory(Path.of(file.getParent()));
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Connection createConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + file.toPath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
