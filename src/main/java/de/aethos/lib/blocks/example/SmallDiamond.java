package de.aethos.lib.blocks.example;

import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.blocks.CustomBlockData;
import de.aethos.lib.blocks.type.BlockType;
import de.aethos.lib.option.Option;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

public class SmallDiamond implements CustomBlock {
    private final CustomBlockData data;

    private final BlockType<SmallDiamond> type;

    public SmallDiamond(BlockType<SmallDiamond> type, CustomBlockData data) {
        this.data = data;
        this.type = type;

    }

    @Override
    public CustomBlockData getCustomBlockData() {
        return data;
    }

    @Override
    public void onPlace(PlayerInteractEvent event) {
        final Vector offset = new Vector(Math.random() - 0.5, -0.5f, Math.random() - 0.5);
        final float yaw = event.getPlayer().getYaw();
        final ItemDisplay display = spawnItemDisplay(yaw);
        display.teleport(display.getLocation().add(offset));
        final Option<Interaction> interaction = spawnInteractionDisplay(yaw);
        interaction.map(ent -> ent.teleport(ent.getLocation().add(offset)));
    }

    @Override
    public void onRemove() {
        despawnItemDisplay();
        despawnInteraction();
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {

    }

    @Override
    public void onSave(ChunkUnloadEvent event) {

    }

    @Override
    public Collection<ItemStack> getDrops() {
        return List.of();
    }

    @Override
    public BlockType<? extends CustomBlock> getBlockType() {
        return type;
    }


}
