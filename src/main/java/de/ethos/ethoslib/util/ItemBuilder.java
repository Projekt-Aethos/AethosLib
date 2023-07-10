package de.ethos.ethoslib.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material){
        this.item = new ItemStack(material);
    }



    public ItemBuilder displayName(Component component){
        final ItemMeta meta = item.getItemMeta();
        meta.displayName(component);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(Component... lore){
        final ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(lore));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLore(@NotNull Component lore){
        final ItemMeta meta = item.getItemMeta();
        List<Component> itemlore= meta.lore();
        if (itemlore != null){
            itemlore.add(lore);
        } else {
            itemlore = List.of(lore);
        }
        meta.lore(itemlore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setPersistentData(String value, NamespacedKey key){
        final ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(key,PersistentDataType.PrimitivePersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build(){
        return item;
    }

    public ItemStack getItem() {
        return build();
    }


}
