package de.aethos.lib.wiki;


import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class WikiSite implements ComponentLike, Keyed, ClickCallback<Audience> {
    private final NamespacedKey key;
    protected Component title;
    protected List<Component> pages;
    protected List<NamespacedKey> nodes = new ArrayList<>();


    public WikiSite(NamespacedKey key, Component title, List<Component> pages) {
        this.key = key;
        this.title = title;
        this.pages = pages;
        Queue<Component> queue = new LinkedList<>(pages);
        Component comp = queue.poll();
        while (comp != null) {
            queue.addAll(comp.children());
            ClickEvent event = comp.clickEvent();
            if (event != null) {
                if (event.action().equals(ClickEvent.Action.RUN_COMMAND)) {
                    String str = event.value();
                    if (str.startsWith("/aethoslib:callback wiki ")) {
                        nodes.add(NamespacedKey.fromString(str.substring("/aethoslib:callback wiki".length())));
                    }
                }
            }
            comp = queue.poll();
        }
    }

    public Book asBook() {
        return Book.builder()
                .author(Component.text("Wiki"))
                .pages(pages)
                .title(title)
                .build();
    }

    @Override
    public @NotNull Component asComponent() {
        final ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> meta.displayName(title));
        item.editMeta(meta -> meta.lore(pages));
        return title.clickEvent(clickEvent()).hoverEvent(item.asHoverEvent());
    }

    public final @NotNull ClickEvent clickEvent() {
        return ClickEvent.runCommand("/aethoslib:callback wiki " + key.toString());
    }

    @Override
    public final @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public void accept(@NotNull Audience audience) {
        audience.openBook(this.asBook());
    }
}
