package de.ethos.ethoslib.inventory.gui;

import de.ethos.ethoslib.inventory.item.EthosItem;
import de.ethos.ethoslib.inventory.item.LightEthosItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

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
    public void onLightEthosItem(final InventoryClickEvent event) {
        LightEthosItem.onKlick(event);
    }

}
