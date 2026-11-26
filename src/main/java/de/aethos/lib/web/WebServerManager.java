package de.aethos.lib.web;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the web servers of the plugins the way the plugin manager manages listeners: a plugin
 * builds its {@link PluginWebServer}, hands it over and is done. The server is started here and
 * stopped when the plugin is disabled, so no plugin needs web code in {@code onDisable}.
 * <p>
 * New, has no template in the plugins. Call from the main thread only.
 *
 * <pre>{@code
 * final PluginWebServer web = PluginWebServer.create("AethosWiki", getLogger(), bind, port, 2);
 * web.route("/", this::root);
 * web.route("/api/save", auth, this::save);
 * AethosLib.getPlugin(AethosLib.class).getWebServerManager().register(this, web);
 * }</pre>
 */
public final class WebServerManager {
    private final Map<Plugin, List<PluginWebServer>> servers = new HashMap<>();

    /**
     * Starts the server and stops it again when the plugin is disabled.
     *
     * @param plugin the plugin that owns the server, must be enabled
     * @param server a server with its routes that has not been started yet
     * @return the server
     * @throws IllegalStateException if the plugin is not enabled or the server was already started
     */
    public synchronized PluginWebServer register(final Plugin plugin, final PluginWebServer server) {
        if (!plugin.isEnabled()) {
            throw new IllegalStateException(plugin.getName() + " registers a web server while not enabled");
        }
        server.start();
        List<PluginWebServer> owned = servers.get(plugin);
        if (owned == null) {
            owned = new ArrayList<>();
            servers.put(plugin, owned);
            plugin.getServer().getPluginManager().registerEvents(new StopOnDisable(plugin), plugin);
        }
        owned.add(server);
        return server;
    }

    /**
     * Stops one server before its plugin is disabled, for example on a reload of the config.
     *
     * @param plugin the plugin that owns the server
     * @param server the server to stop
     * @return true if the server was registered
     */
    public synchronized boolean unregister(final Plugin plugin, final PluginWebServer server) {
        final List<PluginWebServer> owned = servers.get(plugin);
        if (owned == null || !owned.remove(server)) {
            return false;
        }
        server.stop();
        return true;
    }

    /**
     * Stops all servers of a plugin. Happens on its own when the plugin is disabled.
     *
     * @param plugin the plugin that owns the servers
     */
    public synchronized void unregisterAll(final Plugin plugin) {
        final List<PluginWebServer> owned = servers.get(plugin);
        if (owned != null) {
            owned.forEach(PluginWebServer::stop);
            owned.clear();
        }
    }

    /**
     * @param plugin the plugin to look up
     * @return the running servers of the plugin
     */
    public synchronized List<PluginWebServer> of(final Plugin plugin) {
        return List.copyOf(servers.getOrDefault(plugin, List.of()));
    }

    /**
     * @return the running servers of all plugins
     */
    public synchronized Map<Plugin, List<PluginWebServer>> all() {
        final Map<Plugin, List<PluginWebServer>> copy = new HashMap<>();
        servers.forEach((plugin, owned) -> {
            if (!owned.isEmpty()) {
                copy.put(plugin, List.copyOf(owned));
            }
        });
        return Map.copyOf(copy);
    }

    private synchronized void forget(final Plugin plugin) {
        unregisterAll(plugin);
        servers.remove(plugin);
    }

    /**
     * Registered once per plugin; Bukkit removes it together with the plugin's other listeners.
     */
    private final class StopOnDisable implements Listener {
        private final Plugin plugin;

        StopOnDisable(final Plugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onDisable(final PluginDisableEvent event) {
            if (event.getPlugin().equals(plugin)) {
                forget(plugin);
            }
        }
    }
}
