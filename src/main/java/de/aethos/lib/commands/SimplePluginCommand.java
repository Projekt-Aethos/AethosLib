package de.aethos.lib.commands;

import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
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
    public SimplePluginCommand(final String name, final AePlugin plugin) {
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
    @Contract("_, null -> null; _, !null -> !null")
    @Nullable
    protected List<String> complete(final String[] args, @Nullable final List<String> toComplete) {
        if (toComplete == null) {
            return null;
        }
        final String lastArg = args[args.length - 1];
        final List<String> out = new ArrayList<>(toComplete.size());
        for (final String completion : toComplete) {
            if (lastArg.matches(" *") || completion.toLowerCase(Locale.ROOT).startsWith(lastArg.toLowerCase(Locale.ROOT))) {
                out.add(completion);
            }
        }
        return out;
    }

    @Override
    public AePlugin getPlugin() {
        return plugin;
    }
}
