package de.ethos.ethoslib.GUI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public interface Displayable {

    default ItemStack getItem() {
        ItemStack item  = new ItemStack(Material.DIRT);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("ERROR 404", NamedTextColor.YELLOW,TextDecoration.BOLD).decoration(TextDecoration.ITALIC,false));
        item.setItemMeta(meta);
        return item;
    }
    String getName();

    Component displayName();

}
