package de.aethos.lib.data.database;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLBuilder {

    private String user;
    private String database;
    private String password;
    private String port;
    private String hostname;

    public MySQLBuilder user(String user) {
        this.user = user;
        return this;
    }

    public MySQLBuilder database(String database) {
        this.database = database;
        return this;
    }

    public MySQLBuilder password(String password) {
        this.password = password;
        return this;
    }

    public MySQLBuilder port(int i) {
        this.port = String.valueOf(i);
        return this;
    }

    public MySQLBuilder hostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public Database build() {
        MySQL mysql = new MySQL(user, database, password, port, hostname);
        try (Connection connection = mysql.createConnection()) {
            return mysql;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
