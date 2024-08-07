package de.aethos.lib.blocks;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record UndefinedType(CustomBlockData data) implements CustomBlock {


    public String getType() {
        return Objects.requireNonNullElse(data.container().get(CustomBlock.Key.TYPE_KEY, PersistentDataType.STRING), "INVALID");
    }


    @Override
    public CustomBlockData getCustomBlockData() {
        return data;
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

    @Override
    public Collection<ItemStack> getDrops() {
        return List.of();
    }
}
