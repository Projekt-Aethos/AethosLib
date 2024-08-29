package de.aethos.lib.blocks.event;

import de.aethos.lib.blocks.CustomBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class CustomBlockBreakEvent extends BlockBreakEvent {
    protected final CustomBlock custom;

    public CustomBlockBreakEvent(CustomBlock custom, Block block, Player player) {
        super(block, player);
        this.custom = custom;
    }

    public CustomBlock getCustomBlock() {
        return custom;
    }

}
