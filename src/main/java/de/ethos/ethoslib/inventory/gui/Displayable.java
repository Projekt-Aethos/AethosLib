package de.ethos.ethoslib.inventory.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;



public interface Displayable {

    ItemStack getItem();
    String getName();

    Component displayName();


}
