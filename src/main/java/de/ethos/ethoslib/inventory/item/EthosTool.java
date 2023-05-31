package de.ethos.ethoslib.inventory.item;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class EthosTool extends EthosItem {
    public EthosTool(Material material, Component displayName, Component... lore) {
        super(material, displayName, lore);
    }

    public abstract void onKlickBlock(Block block, Player player);

    public void onKlickAir(Player player) {
    }

    public void onHit(Player player) {
    }

}
