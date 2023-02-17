package de.ethos.ethoslib.GUI;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class GUIItem extends ItemStack {

    public GUIItem(Material material, Component displayName, Component... lore){
        super(material);
        ItemMeta meta = this.getItemMeta();
        meta.displayName(displayName);
        meta.lore(List.of(lore));
    }
    public abstract void onKlick(InventoryClickEvent event);



}
