package de.aethos.lib.blocks.example;

import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.blocks.CustomBlockData;
import de.aethos.lib.blocks.type.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public class Muelleimer implements CustomBlock {
    private final CustomBlockData data;
    private final BlockType<Muelleimer> type;

    public Muelleimer(BlockType<Muelleimer> type, CustomBlockData data) {
        this.data = data;
        this.type = type;
    }

    @Override
    public CustomBlockData getCustomBlockData() {
        return data;
    }

    @Override
    public void onPlace(PlayerInteractEvent event) {

    }

    @Override
    public void onRemove() {

    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction().isRightClick()) {
            event.setCancelled(true);
            Inventory inventory = new MyInventory().inventory;
            event.getPlayer().openInventory(inventory);
        }

    }

    @Override
    public void onSave(ChunkUnloadEvent event) {

    }

    @Override
    public Collection<ItemStack> getDrops() {
        return List.of();
    }

    @Override
    public BlockType<? extends CustomBlock> getBlockType() {
        return type;
    }


    public static class MyInventory implements InventoryHolder {

        private final Inventory inventory;

        public MyInventory() {
            this.inventory = Bukkit.getServer().createInventory(this, InventoryType.CHEST);
        }

        @Override
        public Inventory getInventory() {
            return this.inventory;
        }

    }
}
