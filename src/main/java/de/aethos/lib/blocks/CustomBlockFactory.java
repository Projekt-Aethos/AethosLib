package de.aethos.lib.blocks;

import de.aethos.lib.blocks.type.BlockType;

public interface CustomBlockFactory<T extends CustomBlock> {
    default T create(BlockType<T> type, CustomBlockData data) {
        return construct(type, data);
    }


    T construct(BlockType<T> type, CustomBlockData data);


}
