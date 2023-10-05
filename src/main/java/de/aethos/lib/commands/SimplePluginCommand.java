package de.aethos.lib.commands;

import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple implementation of a bukkit command.
 *
 * @param <AePlugin> the plugin to use
 */
public abstract class SimplePluginCommand<AePlugin extends JavaPlugin> extends Command implements PluginIdentifiableCommand {
    protected final AePlugin plugin;

    /**
     * Creates a new command.
     *
     * @param name   the name
     * @param plugin the plugin
     */
    public SimplePluginCommand(String name, AePlugin plugin) {
        super(name);
        this.plugin = plugin;
    }

    /**
     * Filters the given list entries based on the last argument.
     *
     * @param args       to filter the list entries
     * @param toComplete which should be filtered
     * @return list entries matching last arg
     */
    @Contract("_, null -> null")
    protected List<String> complete(String @NotNull [] args, @Nullable List<String> toComplete) {
        if (toComplete == null) {
            return null;
        }
        String lastArg = args[args.length - 1];
        List<String> out = new ArrayList<>(toComplete.size());
        for (String completion : toComplete) {
            if (lastArg.matches(" *") || completion.toLowerCase(Locale.ROOT).startsWith(lastArg.toLowerCase(Locale.ROOT))) {
                out.add(completion);
            }
        }
        return out;
    }

    @NotNull
    @Override
    public AePlugin getPlugin() {
        return plugin;
    }
}
