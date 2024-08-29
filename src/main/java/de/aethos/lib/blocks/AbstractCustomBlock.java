package de.aethos.lib.blocks;

import de.aethos.lib.blocks.type.BlockType;

public abstract class AbstractCustomBlock implements CustomBlock {

    protected final BlockType<? extends AbstractCustomBlock> type;
    protected final CustomBlockData data;


    public AbstractCustomBlock(BlockType<? extends AbstractCustomBlock> type, CustomBlockData data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public final BlockType<? extends CustomBlock> getBlockType() {
        return type;
    }


    @Override
    public final CustomBlockData getCustomBlockData() {
        return data;
    }
}
