package de.aethos.lib;

import de.aethos.lib.registry.Registry;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class ItemRegistry extends Registry<ItemRegistry.Item> {

    public ItemRegistry(final JavaPlugin plugin, final Logger logger) {
        super(plugin, logger);
        for (final ItemType itemType : org.bukkit.Registry.ITEM) {
            register(new VanillaItem(itemType));
        }
    }

    public interface Item extends Keyed {

        ItemStack getItemStack(int count);
    }

    private record VanillaItem(NamespacedKey getKey, ItemType type) implements Item {

        VanillaItem(final ItemType type) {
            this(type.getKey(), type);
        }

        @Override
        public ItemStack getItemStack(final int count) {
            return type.createItemStack(count);
        }
    }
}
