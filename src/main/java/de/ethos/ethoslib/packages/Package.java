package de.ethos.ethoslib.packages;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class Package<P extends JavaPlugin>{


    private P plugin;
    private String name;

    public Package(){

    }


    public final void init(@NotNull P plugin,@NotNull String name){
        this.plugin = plugin;
        this.name = name;

    }

    public abstract void onEnabled();
    public abstract void onDisabled();

    public String getName() {
        return name;
    }

    public P getPlugin(){
        return plugin;
    }


}
