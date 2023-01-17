package de.ethos.ethoslib.GUI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GUIView extends InventoryView {
    private final Menu menu;

    public GUIView(@NotNull Menu menu){
        this.menu = menu;

        menu.player.openInventory(this);
    }

    public UUID getMenuID(){
        return menu.getUUID();
    }

    public Menu getMenu() {
        return menu;
    }

    public String getMenuName(){
        return getTitle();
    }
    @Override
    public @NotNull Inventory getTopInventory() {
        return menu.getInventory();
    }

    @Override
    public @NotNull Inventory getBottomInventory() {
        return menu.player.getInventory();
    }

    @Override
    public @NotNull HumanEntity getPlayer() {
        return menu.player;
    }

    @Override
    public @NotNull InventoryType getType() {
        return InventoryType.CHEST;
    }

    @Override
    public @NotNull String getTitle() {
        return LegacyComponentSerializer.legacyAmpersand().serialize(menu.title);
    }
    public @NotNull Component getTitleComp() {
        return menu.title;
    }
}
