package de.aethos.lib.inventory.gui;

import de.aethos.lib.inventory.item.EthosItem;
import de.aethos.lib.inventory.item.LightEthosItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

@Deprecated
public class GUIListener implements org.bukkit.event.Listener {

    @EventHandler(ignoreCancelled = true)
    public void onGUI(InventoryClickEvent event) {
        if (event.getView() instanceof ProtectedView) {
            event.setCancelled(true);
        }
        if (EthosItem.getItem(event.getCurrentItem()) instanceof GUIItem item) {
            item.onKlick(event);
        }
    }

    @EventHandler
    public void onLightEthosItem(InventoryClickEvent event) {
        LightEthosItem.onKlick(event);
    }

}
