package de.aethos.lib.blocks.event;

import de.aethos.lib.blocks.CustomBlock;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomBlockPlaceEvent extends BlockPlaceEvent {
    private final @NotNull CustomBlock customBlock;

    public CustomBlockPlaceEvent(CustomBlock customBlock, @NotNull BlockState replacedBlockState, @NotNull Block placedAgainst, @NotNull ItemStack itemInHand, @NotNull Player thePlayer, boolean canBuild, @NotNull EquipmentSlot hand) {
        super(customBlock.getCustomBlockData().block(), replacedBlockState, placedAgainst, itemInHand, thePlayer, canBuild, hand);
        this.customBlock = customBlock;
    }


    public @NotNull CustomBlock getCustomBlock() {
        return customBlock;
    }
}
