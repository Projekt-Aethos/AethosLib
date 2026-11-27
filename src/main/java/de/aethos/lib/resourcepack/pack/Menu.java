package de.aethos.lib.resourcepack.pack;

import de.aethos.lib.ui.ComponentMenuHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import xyz.janboerman.guilib.api.menu.ItemButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Menu extends ComponentMenuHolder<Plugin> {

    private static final int PER_PAGE = 18;

    private static final int SIZE = PER_PAGE + 9;

    private final Sender sender;

    private final Player player;

    private final List<Entry> entries;

    private final Map<String, Boolean> draft = new LinkedHashMap<>();

    private int page;

    private boolean accepted;

    Menu(final Plugin plugin, final Sender sender, final Player player, final List<Entry> entries) {
        super(plugin, SIZE, Component.text("Ressourcepacks"));
        this.sender = sender;
        this.player = player;
        this.entries = entries;
        draw();
    }

    private static Component line(final String text, final NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }

    private static ItemStack icon(final Material material, @Nullable final String model, final Component name,
                                  final List<Component> lore) {
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.values());
            if (model != null) {
                meta.setItemModel(new NamespacedKey("aethos", "gui/" + model));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void onClose(final InventoryCloseEvent event) {
        if (!accepted && !draft.isEmpty()) {
            player.sendMessage(line("Auswahl verworfen – nichts geändert.", NamedTextColor.GRAY));
        }
    }

    private boolean state(final Entry entry) {
        return draft.getOrDefault(entry.id(), entry.on());
    }

    private void draw() {
        clearButtons();
        final ItemStack glass = icon(Material.GRAY_STAINED_GLASS_PANE, null, Component.text(" "), List.of());
        for (int slot = PER_PAGE; slot < SIZE; slot++) {
            setButton(slot, new ItemButton<>(glass));
        }
        final int pages = Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.clamp(page, 0, pages - 1);
        final int from = page * PER_PAGE;
        for (int i = from; i < Math.min(entries.size(), from + PER_PAGE); i++) {
            final Entry entry = entries.get(i);
            button(i - from, packIcon(entry), () -> {
                final boolean next = !state(entry);
                if (next == entry.on()) {
                    draft.remove(entry.id());
                } else {
                    draft.put(entry.id(), next);
                }
                draw();
            });
        }
        if (page > 0) {
            button(PER_PAGE, icon(Material.ARROW, "pfeil_links", line("Vorherige Seite", NamedTextColor.GRAY),
                    List.of(line("Seite " + page + " von " + pages, NamedTextColor.DARK_GRAY))), () -> {
                page--;
                draw();
            });
        }
        if (page < pages - 1) {
            button(PER_PAGE + 8, icon(Material.ARROW, "pfeil_rechts", line("Nächste Seite", NamedTextColor.GRAY),
                    List.of(line("Seite " + (page + 2) + " von " + pages, NamedTextColor.DARK_GRAY))), () -> {
                page++;
                draw();
            });
        }
        final boolean changed = !draft.isEmpty();
        button(PER_PAGE + 2, icon(changed ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE, null,
                line("Akzeptieren", changed ? NamedTextColor.GREEN : NamedTextColor.GRAY),
                List.of(line(changed ? draft.size() + (draft.size() == 1 ? " Änderung" : " Änderungen") + " übernehmen"
                                : "Noch nichts geändert", NamedTextColor.GRAY),
                        line("Die Packs werden danach einmal neu geladen.", NamedTextColor.DARK_GRAY))), this::accept);
        setButton(PER_PAGE + 4, new ItemButton<>(icon(Material.PAPER, "info", line("Optionale Ressourcepacks", NamedTextColor.GOLD),
                List.of(line("Diese Packs sind freiwillig.", NamedTextColor.GRAY),
                        line("Klicken merkt die Auswahl nur vor,", NamedTextColor.GRAY),
                        line("erst Akzeptieren übernimmt sie.", NamedTextColor.GRAY),
                        line("Als Text: /pack liste", NamedTextColor.GRAY)))));
        button(PER_PAGE + 6, icon(Material.BARRIER, "schliessen", line("Abbrechen", NamedTextColor.RED),
                List.of(line("Schließen, ohne etwas zu ändern", NamedTextColor.GRAY))), player::closeInventory);
    }

    private ItemStack packIcon(final Entry entry) {
        final boolean on = state(entry);
        final List<Component> lore = new ArrayList<>();
        lore.add(on ? line("eingeschaltet", NamedTextColor.GREEN) : line("ausgeschaltet", NamedTextColor.RED));
        if (draft.containsKey(entry.id())) {
            lore.add(line("Vorgemerkt – noch nicht übernommen", NamedTextColor.YELLOW));
        }
        lore.add(line("Größe: " + entry.size(), NamedTextColor.GRAY));
        if (entry.defaultOn()) {
            lore.add(line("Standardmäßig an", NamedTextColor.GRAY));
        }
        lore.add(Component.empty());
        lore.add(line(on ? "Klick: ausschalten" : "Klick: einschalten", NamedTextColor.YELLOW));
        return icon(on ? Material.LIME_DYE : Material.GRAY_DYE, on ? "pack_an" : "pack_aus",
                line(entry.name(), on ? NamedTextColor.GREEN : NamedTextColor.RED), lore);
    }

    private void accept() {
        if (draft.isEmpty()) {
            return;
        }
        accepted = true;
        final Map<String, Boolean> changes = Map.copyOf(draft);
        player.closeInventory();
        sender.choose(player, changes);
    }

    private void button(final int slot, final ItemStack icon, final Runnable action) {
        setButton(slot, new ItemButton<Menu>(icon) {
            @Override
            public void onClick(final Menu holder, final InventoryClickEvent event) {
                if (!event.getWhoClicked().equals(player)) {
                    return;
                }
                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                    if (player.isOnline()) {
                        action.run();
                    }
                });
            }
        });
    }

    record Entry(String id, String name, String size, boolean on, boolean defaultOn) {
    }
}
