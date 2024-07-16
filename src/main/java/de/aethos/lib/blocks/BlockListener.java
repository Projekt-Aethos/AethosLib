package de.aethos.lib.blocks;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {
    public static final BlockListener INSTANCE = new BlockListener();


    private BlockListener() {

    }

    @EventHandler
    public void onItem(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final Block block = event.getClickedBlock();
        final Player player = event.getPlayer();
        if (block == null || block.getType().isInteractable() && !player.isSneaking()) {
            return;
        }
        final ItemStack handItem = player.getInventory().getItemInMainHand();
        Block place = block.getRelative(event.getBlockFace());
        if (handItem.getItemMeta().getPersistentDataContainer().has(CustomBlock.Key.ITEM_TYPE_KEY)) {
            CustomBlock.create(place, handItem);
        }

    }

}
