package de.aethos.lib.blocks;

import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public abstract class BaseBlock implements CustomBlock {

    private final CustomBlockData data;

    protected BaseBlock(CustomBlockData data) {
        this.data = data;
    }

    @Override
    public CustomBlockData getCustomBlockData() {
        return data;
    }


    @Override
    public void onPlace(PlayerInteractEvent event) {

    }

    @Override
    public void onInteract(PlayerInteractEvent event) {

    }

    @Override
    public void onSave(ChunkUnloadEvent event) {

    }


}
