package de.aethos.lib.ui.item;


import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.janboerman.guilib.api.menu.ItemButton;
import xyz.janboerman.guilib.api.menu.MenuHolder;


public class CallbackButton<MH extends MenuHolder<?>> extends ItemButton<MH> {
    protected final ClickCallback<? super Audience> callback;

    public CallbackButton(@NotNull ItemStack item, @NotNull ClickCallback<? super Audience> callback) {
        super(item);
        this.callback = callback;
    }

    @Override
    public void onClick(MH holder, @NotNull InventoryClickEvent event) {
        callback.accept(event.getWhoClicked());
    }
}
