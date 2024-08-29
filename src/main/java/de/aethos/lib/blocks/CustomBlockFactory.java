package de.aethos.lib.blocks;

import de.aethos.lib.blocks.type.BlockType;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;

public interface CustomBlockFactory<T extends CustomBlock> {
    default T create(BlockType<T> type, CustomBlockData data) {
        return construct(type, data);
    }

    default T place(BlockType<T> type, Block source, CustomBlockData data, PlayerInteractEvent event) {
        final T custom = construct(type, data);
        source.setType(type.vanillaDisplayMaterial());
        custom.onPlace(event);
        return custom;
    }


    T construct(BlockType<T> type, CustomBlockData data);


}
