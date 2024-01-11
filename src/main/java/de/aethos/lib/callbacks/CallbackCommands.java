package de.aethos.lib.callbacks;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CallbackCommands extends Command {
    private final Map<String, CallBackResolver> map = new ConcurrentHashMap<>();

    public CallbackCommands() {
        super("callback");
    }

    public void add(String key, CallBackResolver resolver) {
        map.put(key, resolver);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 2) {
            final ClickCallback<Audience> callback = map.getOrDefault(args[0], strings -> null).apply(args[1]);
            if (callback == null) {
                return false;
            }
            callback.accept(sender);
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return map.keySet().stream().toList();
        }
        if (args.length == 2 && map.containsKey(args[0])) {
            return map.get(args[0]).tabCompletion(args[1]);
        }
        return List.of();
    }
}
