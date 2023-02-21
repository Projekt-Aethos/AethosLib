package de.ethos.ethoslib.GUI;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements org.bukkit.event.Listener {

    @EventHandler(ignoreCancelled = true)
    public void onGUI(InventoryClickEvent event){
        if(event.getView() instanceof ProtectedView){
            event.setCancelled(true);
        }
        GUIItem item = GUIItem.getItem(event.getCurrentItem());
        if(item != null){
            item.onKlick(event);
        }

    }

}
