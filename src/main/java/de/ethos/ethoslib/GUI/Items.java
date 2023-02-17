package de.ethos.ethoslib.GUI;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;


public class Items {

    public static final GUIItem PLACEHOLDER = new GUIItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(".", NamedTextColor.BLACK)) {
        @Override
        public void onKlick(InventoryClickEvent event) {
            event.setCancelled(true);
        }
    };
    public static final GUIItem EXIT = new GUIItem(Material.BARRIER, Component.text("Schließen", NamedTextColor.DARK_RED)) {
        @Override
        public void onKlick(InventoryClickEvent event) {
            event.setCancelled(true);
        }
    };


}
