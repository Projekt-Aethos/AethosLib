package de.aethos.lib.blocks;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.NotNull;

public record CustomBlockData(Block block, NamespacedKey key,
                              PersistentDataContainer container) implements PersistentDataHolder {
    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return container;
    }
}
