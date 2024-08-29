package de.aethos.lib.blocks.event.listener;

import de.aethos.lib.AethosLib;
import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.blocks.event.CustomBlockPlaceEvent;
import de.aethos.lib.blocks.type.BlockType;
import de.aethos.lib.option.Some;
import de.aethos.lib.result.Error;
import de.aethos.lib.result.Okay;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class CustomBlockPlaceListener implements Listener {
    public static final CustomBlockPlaceListener INSTANCE = new CustomBlockPlaceListener();

    private CustomBlockPlaceListener() {

    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event instanceof CustomBlockPlaceEvent) {
            return;
        }
        event.setCancelled(CustomBlock.exists(event.getBlock()));
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlace(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        final Player player = event.getPlayer();
        if (clickedBlock == null) {
            return;
        }
        if (clickedBlock.getType().isInteractable() && !player.isSneaking()) {
            return;
        }
        if (event.getAction().isLeftClick()) {
            return;
        }

        final ItemStack handItem = player.getInventory().getItemInMainHand();

        final BlockFace face = event.getBlockFace();
        final Block block = clickedBlock.getRelative(face);

        if (!block.getType().equals(Material.AIR)) {
            return;
        }
        if (!handItem.hasItemMeta()) {
            return;
        }
        if (!handItem.getItemMeta().getPersistentDataContainer().has(BlockType.Key.TYPE_KEY)) {
            return;
        }
        if (BlockType.Register.from(handItem) instanceof Some<BlockType<CustomBlock>> some) {
            BlockType<CustomBlock> type = some.value();


            var res = CustomBlock.place(block, type, event);
            if (res instanceof Okay<CustomBlock, Exception> okay) {
                AethosLib.getPlugin(AethosLib.class).getLogger().info("CustomBlock " + okay.value().getCustomBlockData().key());
                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    handItem.setAmount(handItem.getAmount() - 1);
                }
            } else if (res instanceof Error<CustomBlock, Exception> error) {
                AethosLib.getPlugin(AethosLib.class).getLogger().info(error.toString());
            }


        }


    }

}
