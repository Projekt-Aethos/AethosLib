package de.aethos.lib.packages;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class Package<P extends JavaPlugin> {
    private P plugin;

    private String name;

    public Package() {

    }

    public final void init(final P plugin, final String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public abstract void onEnabled();

    public abstract void onDisabled();

    public String getName() {
        return name;
    }

    public P getPlugin() {
        return plugin;
    }
}
