package de.aethos.lib.data.database.connector;

import de.aethos.lib.data.database.Database;
import de.aethos.lib.data.database.MySQLBuilder;
import de.aethos.lib.data.database.SQLiteBuilder;
import de.aethos.lib.data.database.pool.ConnectionPool;
import de.aethos.lib.data.database.pool.QueuedConnectionPool;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DefaultPluginConnector extends PoolConnector {
    private final JavaPlugin plugin;

    public DefaultPluginConnector(final JavaPlugin plugin) {
        super(parseConfig(plugin));
        this.plugin = plugin;
    }

    private static ConnectionPool parseConfig(final JavaPlugin plugin) {
        final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        if (configuration.isSet("mysql")) {
            final Database database = new MySQLBuilder()
                    .hostname(configuration.getString("mysql.hostname"))
                    .port(configuration.getInt("mysql.port"))
                    .user(configuration.getString("mysql.user"))
                    .password(configuration.getString("mysql.password"))
                    .database(configuration.getString("mysql.database"))
                    .build();
            return new QueuedConnectionPool(database, Math.max(configuration.getInt("mysql.poolsize"), 1), plugin.getLogger());
        }
        if (configuration.isSet("sqlite")) {
            final File file = new File(plugin.getDataFolder(), Objects.requireNonNull(configuration.getString("sqlite.name")) + ".sqlite");
            if (!file.exists()) {
                if (configuration.isSet("sqlite.create") && configuration.getBoolean("sqlite.create")) {
                    try {
                        file.createNewFile();
                    } catch (final IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            final Database database = new SQLiteBuilder()
                    .file(file)
                    .build();
            return new QueuedConnectionPool(database, Math.max(configuration.getInt("sqlite.poolsize"), 1), plugin.getLogger());
        }
        throw new IllegalStateException("Has to set up db information in config.yml");
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
