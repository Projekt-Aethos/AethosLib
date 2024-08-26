package de.aethos.lib.ui.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemBuilder {
    private final ItemStack item;

    public ItemBuilder(final Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder displayName(final Component component) {
        item.editMeta(meta -> meta.displayName(component));
        return this;
    }

    public ItemBuilder lore(final Component... lore) {
        final ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(lore));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLore(final Component lore) {
        final ItemMeta meta = item.getItemMeta();
        List<Component> itemlore = meta.lore();
        if (itemlore != null) {
            itemlore.add(lore);
        } else {
            itemlore = List.of(lore);
        }
        meta.lore(itemlore);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setPersistentData(final String value, final NamespacedKey key) {
        item.editMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.PrimitivePersistentDataType.STRING, value));
        return this;
    }

    public ItemStack build() {
        return item;
    }

    public ItemBuilder setCustomModelData(final int i) {
        item.editMeta(meta -> meta.setCustomModelData(i));
        return this;
    }

    public ItemStack getItem() {
        return build();
    }
}
