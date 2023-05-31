package de.ethos.ethoslib.inventory.gui;

import de.ethos.ethoslib.inventory.item.EthosItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public abstract class GUIItem extends EthosItem {

    public GUIItem(Material material, Component text, Component... components) {
        super(material, text, components);
    }

    @Override
    public abstract void onKlick(@NotNull InventoryClickEvent event);

}


