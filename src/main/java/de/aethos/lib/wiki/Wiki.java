package de.aethos.lib.wiki;


import de.aethos.lib.callbacks.CallBackResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wiki implements CallBackResolver {
    protected final Map<NamespacedKey, WikiSite> map = new HashMap<>();

    public Wiki(JavaPlugin plugin) {
        WikiSite note = new WikiSite(new NamespacedKey(plugin, "Second"), Component.text("Title2"), List.of(Component.text("Dies ist eine zweite Seite")));
        register(note);
        register(new WikiSite(new NamespacedKey(plugin, "Test"), Component.text("Title"), List.of(Component.text("Test odert so").clickEvent(note.clickEvent()))));

    }

    public void register(WikiSite note) {
        map.put(note.getKey(), note);
    }

    WikiSite get(NamespacedKey key) {
        return map.get(key);
    }

    @Override
    public ClickCallback<Audience> apply(String string) {
        return map.get(NamespacedKey.fromString(string));
    }

    @Override
    public List<String> tabCompletion(String str) {
        return map.keySet().stream().map(NamespacedKey::toString).filter(s -> s.contains(str)).toList();
    }
}
