package de.aethos.lib.resourcepack.pack;

import de.aethos.lib.resourcepack.ResourcePackManager;
import de.aethos.lib.resourcepack.ResourcePackManager.Layer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class Sender implements Listener {

    private static final Pattern EFFECT_ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    private final Plugin plugin;

    private final ResourcePackManager manager;

    private final CountDownLatch firstBuild = new CountDownLatch(1);

    private final Map<UUID, Map<String, Effect>> effects = new HashMap<>();

    private Choices choices;

    private int accepted;

    private int declined;

    private int failed;

    public Sender(final Plugin plugin, final ResourcePackManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private static boolean matches(final String pattern, final String text) {
        return text.matches(Pattern.quote(pattern.toLowerCase(Locale.ROOT)).replace("*", "\\E.*\\Q"));
    }

    private static Set<String> allEffects(final Layer layer) {
        final Set<String> all = new LinkedHashSet<>(layer.biomeEffects().keySet());
        if (layer.postEffect() != null) {
            all.add(layer.postEffect());
        }
        return all;
    }

    public void start(final Path choicesFile) {
        choices = new Choices(choicesFile, plugin.getLogger());
        choices.load();
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tickEffects, 100L, 20L);
    }

    public void ready() {
        firstBuild.countDown();
    }

    public int send(final Player player) {
        final ResourcePackManager.Settings settings = manager.settings();
        if (!settings.enabled()) {
            return 0;
        }
        final List<Artifact> ready = manager.sendable().stream()
                .filter(artifact -> sendsTo(player.getUniqueId(), artifact)).toList();
        if (ready.isEmpty()) {
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

    public boolean isOptional(final Artifact artifact) {
        final Layer layer = manager.layerOf(artifact);
        return layer != null && layer.optional();
    }

    public boolean isDefaultOn(final Artifact artifact) {
        final Layer layer = manager.layerOf(artifact);
        return layer != null && layer.onByDefault();
    }

    public List<Artifact> optional() {
        return manager.sendable().stream().filter(this::isOptional).toList();
    }

    public boolean wants(final UUID player, final Artifact artifact) {
        return choices.isOn(player, artifact.id(), isDefaultOn(artifact));
    }

    public void markDecided(final Player player) {
        choices.markDecided(player.getUniqueId());
    }

    public void openMenu(final Player player) {
        final List<Menu.Entry> entries = new ArrayList<>();
        for (final Artifact artifact : optional()) {
            entries.add(new Menu.Entry(artifact.id(), artifact.name(), artifact.sizeHuman(),
                    wants(player.getUniqueId(), artifact), isDefaultOn(artifact)));
        }
        player.openInventory(new Menu(plugin, this, player, entries).getInventory());
    }

    public int choose(final Player player, final Map<String, Boolean> packs) {
        final Map<String, Boolean> changes = new HashMap<>();
        final List<String> names = new ArrayList<>();
        for (final Artifact artifact : optional()) {
            final Boolean value = packs.get(artifact.id());
            if (value == null || wants(player.getUniqueId(), artifact) == value) {
                continue;
            }
            changes.put(artifact.id(), value);
            names.add(artifact.name() + (value ? " an" : " aus"));
            if (!value) {
                effect(player, artifact, false);
            }
        }
        if (changes.isEmpty()) {
            player.sendMessage(Component.text("Die Auswahl ist bereits so eingestellt.", NamedTextColor.GRAY));
            return 0;
        }
        choices.set(player.getUniqueId(), changes);
        if (send(player) == 0) {
            player.clearResourcePacks();
        }
        player.sendMessage(Component.text("Ressourcepacks geändert (" + String.join(", ", names)
                + ") – die Packs werden neu geladen.", NamedTextColor.GREEN));
        return changes.size();
    }

    @EventHandler
    public void onPreLogin(final AsyncPlayerPreLoginEvent event) {
        final ResourcePackManager.Settings settings = manager.settings();
        if (!settings.enabled() || !settings.applyOnJoin() || !settings.waitForPack() || firstBuild.getCount() == 0) {
            return;
        }
        boolean ready = false;
        try {
            ready = firstBuild.await(settings.waitSeconds(), TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (ready) {
            return;
        }
        plugin.getLogger().info("[Pack] " + event.getName() + " abgewiesen – Ressourcepack noch nicht gebaut.");
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                Component.text("Der Server startet noch – das Ressourcepack wird gerade vorbereitet.\n", NamedTextColor.YELLOW)
                        .append(Component.text("Bitte in ein paar Sekunden erneut verbinden.", NamedTextColor.GRAY)));
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final ResourcePackManager.Settings settings = manager.settings();
        if (!settings.enabled() || !settings.applyOnJoin()) {
            return;
        }
        final Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            send(player);
            final int count = optional().size();
            if (count > 0 && !choices.hasDecided(player.getUniqueId())) {
                player.sendMessage(Component.text("Es gibt " + count
                                + (count == 1 ? " optionales Ressourcepack – " : " optionale Ressourcepacks – "), NamedTextColor.GRAY)
                        .append(Component.text("[/pack]", NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.runCommand("/pack"))
                                .hoverEvent(HoverEvent.showText(Component.text("Klick zeigt die Auswahl")))));
            }
        }, 20L);
    }

    @EventHandler
    public void onStatus(final PlayerResourcePackStatusEvent event) {
        if (!manager.isEnabled()) {
            return;
        }
        final Artifact artifact = manager.artifacts().stream()
                .filter(candidate -> candidate.uuid().equals(event.getID())).findFirst().orElse(null);
        if (artifact == null) {
            return;
        }
        final Player player = event.getPlayer();
        plugin.getLogger().info("[Pack] " + player.getName() + " – " + artifact.name() + " → " + event.getStatus());
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED -> {
                accepted++;
                effect(player, artifact, true);
            }
            case ACCEPTED -> accepted++;
            case DECLINED -> declined++;
            case FAILED_DOWNLOAD, FAILED_RELOAD, INVALID_URL -> {
                failed++;
                if (manager.settings().required() && isOptional(artifact) && wants(player.getUniqueId(), artifact)) {
                    choices.set(player.getUniqueId(), Map.of(artifact.id(), false));
                    plugin.getLogger().warning("[Pack] Optionales Pack '" + artifact.name() + "' für "
                            + player.getName() + " abgewählt (Ladefehler).");
                }
            }
            default -> {
            }
        }
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        effects.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDisable(final PluginDisableEvent event) {
        manager.unregister(event.getPlugin());
    }

    private boolean sendsTo(final UUID player, final Artifact artifact) {
        final Layer layer = manager.layerOf(artifact);
        if (layer == null || layer.follows() == null) {
            return !isOptional(artifact) || wants(player, artifact);
        }
        final Layer leader = manager.leaderOf(layer);
        if (leader == null) {
            return false;
        }
        for (final Artifact candidate : manager.sendable()) {
            final Layer other = manager.layerOf(candidate);
            if (other != null && other.same(leader)) {
                return !isOptional(candidate) || wants(player, candidate);
            }
        }
        return false;
    }

    private void effect(final Player player, final Artifact artifact, final boolean on) {
        final Layer layer = manager.layerOf(artifact);
        if (layer == null || layer.postEffect() == null || !EFFECT_ID.matcher(layer.postEffect()).matches()) {
            return;
        }
        final Map<String, Effect> mine = effects.computeIfAbsent(player.getUniqueId(), key -> new HashMap<>());
        if (on) {
            switchEffect(player, mine.computeIfAbsent(artifact.id(), key -> new Effect()), desired(player, layer));
            return;
        }
        final Effect state = mine.remove(artifact.id());
        if (state == null) {
            allEffects(layer).forEach(id -> command("remove", player, id));
        } else {
            switchEffect(player, state, null);
        }
    }

    private void command(final String verb, final Player player, @Nullable final String effect) {
        if (effect != null && EFFECT_ID.matcher(effect).matches()) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                    "posteffect " + verb + " " + player.getName() + " " + effect);
        }
    }

    private void switchEffect(final Player player, final Effect state, @Nullable final String target) {
        if (Objects.equals(state.current, target)) {
            return;
        }
        command("remove", player, state.current);
        command("add", player, target);
        state.current = target;
        state.candidate = null;
        state.seen = 0;
    }

    @Nullable
    private String desired(final Player player, final Layer layer) {
        if (layer.biomeEffects().isEmpty()) {
            return layer.postEffect();
        }
        final Location location = player.getLocation();
        final String biome = location.getWorld().getBiome(location).getKey().toString();
        final String path = biome.substring(biome.indexOf(':') + 1);
        for (final Map.Entry<String, List<String>> entry : layer.biomeEffects().entrySet()) {
            for (final String pattern : entry.getValue()) {
                if (matches(pattern, biome) || matches(pattern, path)) {
                    return entry.getKey();
                }
            }
        }
        return layer.postEffect();
    }

    private void tickEffects() {
        final Iterator<Map.Entry<UUID, Map<String, Effect>>> players = effects.entrySet().iterator();
        while (players.hasNext()) {
            final Map.Entry<UUID, Map<String, Effect>> entry = players.next();
            final Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player == null || !player.isOnline()) {
                players.remove();
                continue;
            }
            for (final Artifact artifact : manager.artifacts()) {
                final Effect state = entry.getValue().get(artifact.id());
                final Layer layer = manager.layerOf(artifact);
                if (state == null || layer == null || layer.postEffect() == null || layer.biomeEffects().isEmpty()) {
                    continue;
                }
                final String wanted = desired(player, layer);
                if (Objects.equals(wanted, state.current)) {
                    state.candidate = null;
                    state.seen = 0;
                } else if (Objects.equals(wanted, state.candidate)) {
                    if (++state.seen >= 3) {
                        switchEffect(player, state, wanted);
                    }
                } else {
                    state.candidate = wanted;
                    state.seen = 1;
                }
            }
        }
    }

    private static final class Effect {

        @Nullable
        private String current;

        @Nullable
        private String candidate;

        private int seen;
    }
}
