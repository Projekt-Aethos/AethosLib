package de.ethos.ethoslib.inventory.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Menu implements Displayable {
    final Inventory inventory;
    final Player player;
    final Component title;
    //UUID des Menus dient nur der unterscheidung zu anderen Menüs
    protected final UUID uuid;

    protected Menu(@NotNull Player player, @NotNull Component title, int rows) {
        this.player = player;
        this.inventory = Bukkit.createInventory(player, rows * 9, title);
        this.title = title;
        this.uuid = UUID.randomUUID();
        inventory.setItem(8, Items.EXIT);
    }

    public UUID getUUID() {
        return uuid;
    }
    protected void open(Player player) {
        new View(this,player);
    }
    protected void open() {
        new View(this,player);
    }
    protected void initRahmen() {
        int[] indexes_gray = {0, 1, 2, 3, 5, 6, 7, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
        setItem(indexes_gray, Items.PLACEHOLDER);
    }
    protected void setItem(int[] indexes, ItemStack item) {
        for (int i : indexes)
            inventory.setItem(i, item);
    }

    public Inventory getInventory() {
        return inventory;
    }

}
