package de.aethos.lib.blocks.type.data;

import de.aethos.lib.AethosLib;
import de.aethos.lib.blocks.type.BlockType;
import de.aethos.lib.data.AethosDataType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public record ItemData(Material material, int cmd, Component component, NamespacedKey key,
                       Consumer<ItemMeta> consumer) {

    public ItemStack createItem() {
        final ItemStack stack = new ItemStack(material);
        stack.editMeta(meta -> meta.setCustomModelData(cmd));
        stack.editMeta(meta -> meta.displayName(component));
        stack.editMeta(meta -> meta.getPersistentDataContainer().set(BlockType.Key.TYPE_KEY, AethosDataType.NAMESPACED_KEY, key));
        AethosLib.getPlugin(AethosLib.class).getLogger().info("Created " + stack.getType() + " " + stack.getItemMeta().getDisplayName());
        return stack;
    }
}
