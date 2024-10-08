package de.aethos.lib.data.database;

import java.io.File;

public class SQLiteBuilder {
    private File file;

    public SQLiteBuilder file(final File file) {
        if (!file.toPath().toString().endsWith(".sqlite")) {
            throw new IllegalStateException("File should end with .sqlite");
        }
        if (!file.exists()) {
            throw new IllegalStateException("File " + file.getPath() + "does not exists");
        }
        this.file = file;
        return this;
    }

    public SQLite build() {
        return new SQLite(file);
    }
}
