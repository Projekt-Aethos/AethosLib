package de.aethos.lib.cache;

import org.apache.commons.collections4.Transformer;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.function.Consumer;

public class PlayerCache<V> extends Cache<OfflinePlayer, V> implements Listener {
    public PlayerCache(int size, Transformer<? super OfflinePlayer, ? extends V> factory, Consumer<Entry<OfflinePlayer, V>> uncache) {
        super(size, factory, uncache);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        precache(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        clear(event.getPlayer());
    }

}
