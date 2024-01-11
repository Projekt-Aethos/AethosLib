package de.aethos.lib.ui.item;

import net.kyori.adventure.inventory.Book;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;
import xyz.janboerman.guilib.api.menu.ItemButton;
import xyz.janboerman.guilib.api.menu.MenuHolder;

public class BookButton<MH extends MenuHolder<?>> extends ItemButton<MH> {
    public final Book book;

    public BookButton(@NotNull ItemStack item, @NotNull Book book) {
        super(item);
        this.book = book;
    }

    public BookButton(@NotNull ItemStack item) {
        if (item.getItemMeta() instanceof BookMeta meta) {
            this.stack = item.clone();
            this.book = meta;
            return;
        }
        throw new IllegalStateException("Item should be a book");
    }

    public void onClick(MH holder, InventoryClickEvent event) {
        holder.getPlugin().getServer().getScheduler().runTask(holder.getPlugin(), () -> {
            event.getView().close();
            HumanEntity player = event.getWhoClicked();
            player.openBook(book);
        });
    }
}
