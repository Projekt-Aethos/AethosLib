package de.ethos.ethoslib.inventory.gui;

import de.ethos.ethoslib.inventory.item.EthosItem;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public abstract class GUIItem extends EthosItem {


    public GUIItem(Material material, TextComponent text) {
        super(material,text);
    }

    public abstract void onKlick(@NotNull InventoryClickEvent event);


}


