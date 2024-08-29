package de.aethos.lib.blocks;

public interface CustomBlockFactory<T extends CustomBlock> {
    default T create(CustomBlockData data) {
        return construct(data);
    }


    T construct(CustomBlockData data);


}
