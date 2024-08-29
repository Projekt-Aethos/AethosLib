package de.aethos.lib.blocks.example;

import de.aethos.lib.blocks.AbstractCustomBlock;
import de.aethos.lib.blocks.CustomBlockData;
import de.aethos.lib.blocks.type.BlockType;
import org.bukkit.entity.Interaction;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Chair extends AbstractCustomBlock {


    public Chair(BlockType<Chair> type, CustomBlockData data) {
        super(type, data);
    }


    @Override
    public void onPlace(PlayerInteractEvent event) {
        float yaw = event.getPlayer().getYaw();
        spawnItemDisplay(yaw);
        spawnInteractionDisplay(yaw);

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
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().equals(getCustomBlockData().getPersistentDataContainer().get(INTERACTION_KEY, INTERACTION_DATA_TYPE))) {
            Interaction interaction = Objects.requireNonNull(getCustomBlockData().getPersistentDataContainer().get(INTERACTION_KEY, INTERACTION_DATA_TYPE));
            event.getPlayer().sendMessage("Du hast einen Stuhl angeklickt");
            interaction.addPassenger(event.getPlayer());
        }
    }

    @Override
    public void onSave(ChunkUnloadEvent event) {

    }

    @Override
    public Collection<ItemStack> getDrops() {
        return List.of();
    }


}
