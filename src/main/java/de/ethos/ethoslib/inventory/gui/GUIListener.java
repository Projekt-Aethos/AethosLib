package de.ethos.ethoslib.inventory.gui;

import de.ethos.ethoslib.inventory.item.EthosItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements org.bukkit.event.Listener {

    @EventHandler(ignoreCancelled = true)
    public void onGUI(InventoryClickEvent event){
        if(event.getView() instanceof ProtectedView){
            event.setCancelled(true);
        }
        if(EthosItem.getItem(event.getCurrentItem()) instanceof GUIItem item){
            item.onKlick(event);
        }


    }


}
