package de.aethos.lib.blocks;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;

public interface CustomBlockFactory<T extends CustomBlock> {
    default T create(Block block, NamespacedKey key, PersistentDataContainer container) {
        return construct(block, key, container);
    }

    T construct(Block block, NamespacedKey key, PersistentDataContainer container);

}
