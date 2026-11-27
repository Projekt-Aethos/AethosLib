package de.aethos.lib.resourcepack.pack;

import de.aethos.lib.resourcepack.ResourcePackManager;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class Sender implements Listener {

    private final Plugin plugin;

    private final ResourcePackManager manager;

    private int accepted;

    private int declined;

    private int failed;

    public Sender(final Plugin plugin, final ResourcePackManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public int send(final Player player) {
        final ResourcePackManager.Settings settings = manager.settings();
        final List<Artifact> ready = manager.sendable();
        if (!settings.enabled() || ready.isEmpty()) {
            return 0;
        }
        final List<ResourcePackInfo> packs = new ArrayList<>();
        for (final Artifact artifact : ready.reversed()) {
            packs.add(ResourcePackInfo.resourcePackInfo()
                    .id(artifact.uuid())
                    .uri(URI.create(manager.packUrl(artifact)))
                    .hash(artifact.sha1Hex())
                    .build());
        }
        player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                .packs(packs)
                .prompt(Component.text(settings.prompt()))
                .required(settings.required())
                .replace(true)
                .build());
        return packs.size();
    }

    public String counts() {
        return accepted + "/" + declined + "/" + failed;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final ResourcePackManager.Settings settings = manager.settings();
        if (!settings.enabled() || !settings.applyOnJoin()) {
            return;
        }
        final Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                send(player);
            }
        }, 20L);
    }

    @EventHandler
    public void onStatus(final PlayerResourcePackStatusEvent event) {
        if (!manager.isEnabled()) {
            return;
        }
        final String pack = manager.artifacts().stream()
                .filter(artifact -> artifact.uuid().equals(event.getID()))
                .map(Artifact::name).findFirst().orElse(null);
        if (pack == null) {
            return;
        }
        plugin.getLogger().info("[Pack] " + event.getPlayer().getName() + " – " + pack + " → " + event.getStatus());
        switch (event.getStatus()) {
            case ACCEPTED, SUCCESSFULLY_LOADED -> accepted++;
            case DECLINED -> declined++;
            case FAILED_DOWNLOAD, FAILED_RELOAD, INVALID_URL -> failed++;
            default -> {
            }
        }
    }

    @EventHandler
    public void onDisable(final PluginDisableEvent event) {
        manager.unregister(event.getPlugin());
    }
}
