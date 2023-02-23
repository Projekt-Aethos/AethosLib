package de.ethos.ethoslib.inventory.item;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.player.PlayerInteractEvent;

public class ToolListener implements Listener {
    @EventHandler
    public void onToolClick(PlayerInteractEvent event){
        if(EthosItem.getItem(event.getItem()) instanceof EthosTool tool){
            event.setCancelled(true);
          switch (event.getAction()){
              case LEFT_CLICK_BLOCK,RIGHT_CLICK_BLOCK -> tool.onKlickBlock(event.getClickedBlock(),event.getPlayer());
              case LEFT_CLICK_AIR,RIGHT_CLICK_AIR -> tool.onKlickAir(event.getPlayer());
              case PHYSICAL -> tool.onHit(event.getPlayer());
          }

        }

    }
}
