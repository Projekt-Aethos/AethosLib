package de.aethos.lib.data.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public record MySQL(String hostname, String port, String database, String user, String password) implements Database {
    public MySQL {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Connection createConnection() {
        try {
            return DriverManager.getConnection("jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database + "?&useSSL=false",
                    this.user, this.password);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
