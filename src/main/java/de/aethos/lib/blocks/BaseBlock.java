package de.aethos.lib.blocks;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public abstract class BaseBlock implements CustomBlock {

    protected final Block block;
    protected final NamespacedKey key;
    protected final PersistentDataContainer container;

    protected BaseBlock(Block block, NamespacedKey key, PersistentDataContainer container) {
        this.block = block;
        this.key = key;
        this.container = container;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return container;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public void onCreate(BlockPlaceEvent event) {

    }

    @Override
    public void onInteract(PlayerInteractEvent event) {

    }

    @Override
    public void onSave(ChunkUnloadEvent event) {

    }

    @Override
    public void onBreak(BlockBreakEvent event) {

    }
}
