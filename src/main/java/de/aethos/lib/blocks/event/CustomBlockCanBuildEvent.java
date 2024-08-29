package de.aethos.lib.blocks.event;

import de.aethos.lib.blocks.CustomBlock;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomBlockCanBuildEvent extends BlockCanBuildEvent {
    private final CustomBlock custom;

    public CustomBlockCanBuildEvent(CustomBlock block, @Nullable Player player, @NotNull BlockData type, boolean canBuild, @NotNull EquipmentSlot hand) {
        super(block.getCustomBlockData().block(), player, type, canBuild, hand);
        this.custom = block;
    }

    public CustomBlock getCustomBlock() {
        return custom;
    }
}
