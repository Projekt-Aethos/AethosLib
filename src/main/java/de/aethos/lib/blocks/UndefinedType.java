package de.aethos.lib.blocks;

import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record UndefinedType(Block block, NamespacedKey key, PersistentDataContainer container) implements CustomBlock {


    public String getType() {
        return Objects.requireNonNullElse(container.get(CustomBlocks.Key.TYPE_KEY, PersistentDataType.STRING), "INVALID");
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
    public void onInteract(BlockInteractEvent event) {

    }

    @Override
    public void onSave(ChunkUnloadEvent event) {

    }

    @Override
    public void onBreak(BlockBreakEvent event) {

    }
}
