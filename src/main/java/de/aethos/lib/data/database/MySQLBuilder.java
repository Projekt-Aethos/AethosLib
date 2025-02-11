package de.aethos.lib.data.database;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLBuilder {
    private String user;

    private String database;

    private String password;

    private String port;

    private String hostname;

    public MySQLBuilder user(final String user) {
        this.user = user;
        return this;
    }

    public MySQLBuilder database(final String database) {
        this.database = database;
        return this;
    }

    public MySQLBuilder password(final String password) {
        this.password = password;
        return this;
    }

    public MySQLBuilder port(final int i) {
        this.port = String.valueOf(i);
        return this;
    }

    public MySQLBuilder hostname(final String hostname) {
        this.hostname = hostname;
        return this;
    }

    public Database build() {
        final MySQL mysql = new MySQL(hostname, port, database, user, password);
        try (Connection ignored = mysql.createConnection()) {
            return mysql;
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
