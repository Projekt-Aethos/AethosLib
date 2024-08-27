package de.aethos.lib.items;

import org.bukkit.Keyed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Does something when on {@link ItemStack}s.
 */
public interface ItemFlag extends Keyed {
    /**
     * Simply sets the Flag on the item, without additional data.
     *
     * @param stack the item stack to add the flag to
     */
    default void set(final ItemStack stack) {
        stack.editMeta(itemMeta -> itemMeta.getPersistentDataContainer().set(getKey(), PersistentDataType.BOOLEAN, true));
    }
}
