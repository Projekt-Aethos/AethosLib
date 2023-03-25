package de.ethos.ethoslib.testing.placeholder;


import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;


public class Placeholder extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final String name;
    private final BiFunction<OfflinePlayer,String,String> function;

    public Placeholder(@NotNull JavaPlugin plugin,@NotNull String name,@NotNull BiFunction<OfflinePlayer,@NotNull String,String> function ){
        super();
        this.plugin = plugin;
        this.name = name;
        this.function = function;
    }

    public JavaPlugin getProviderPlugin(){
        return plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName()+":"+name;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getName();
    }

    @Override
    public @NotNull String getVersion() {
        return "10";
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return plugin.getName();
    }

    @Override
    public boolean register() {
        return this.getPlaceholderAPI().getLocalExpansionManager().register(this);
    }

    @Override
    public boolean canRegister() {
        return plugin != null;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return function.apply(player, params); // Placeholder is unknown by the Expansion
    }
}

