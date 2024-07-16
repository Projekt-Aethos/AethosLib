package de.aethos.lib.blocks;

import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@SuppressWarnings("unused")
public interface CustomBlock extends PersistentDataHolder {


    Block getBlock();

    @Override
    @NotNull PersistentDataContainer getPersistentDataContainer();

    NamespacedKey getKey();

    default void remove() {
        getBlock().getChunk().getPersistentDataContainer().remove(getKey());
    }

    void onCreate(BlockPlaceEvent event);

    void onInteract(BlockInteractEvent event);


    void onSave(ChunkUnloadEvent event);

    void onBreak(BlockBreakEvent event);

    default Collection<ItemStack> getDrops() {
        return getBlock().getDrops();
    }

    default Collection<ItemStack> getDrops(ItemStack tool) {
        return getBlock().getDrops(tool);
    }

    default Collection<ItemStack> getDrops(ItemStack tool, Entity entity) {
        return getBlock().getDrops(tool, entity);
    }


}
