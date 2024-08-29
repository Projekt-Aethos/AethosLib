package de.aethos.lib.blocks.event.listener;

import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.option.Some;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class CustomBlockInteractListener implements Listener {
    public static final CustomBlockInteractListener INSTANCE = new CustomBlockInteractListener();

    private CustomBlockInteractListener() {

    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (CustomBlock.get(event.getClickedBlock()) instanceof Some<? extends CustomBlock> some) {
            some.value().onInteract(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (CustomBlock.get(event.getRightClicked()) instanceof Some<? extends CustomBlock> some) {
            some.value().onEntityInteract(event);
        }
    }
}
