package de.aethos.lib.blocks;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record CustomBlockData(Block block, NamespacedKey key,
                              PersistentDataContainer container) implements PersistentDataHolder {
    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return container;
    }

    public void edit(Consumer<PersistentDataContainer> consumer) {
        consumer.accept(container);
        block.getChunk().getPersistentDataContainer().set(key, PersistentDataType.TAG_CONTAINER, container);
    }

    
}
