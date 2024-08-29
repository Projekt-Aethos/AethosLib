package de.aethos.lib.blocks.example;

import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.blocks.CustomBlockData;
import org.bukkit.entity.Interaction;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public class Chair implements CustomBlock {

    private final CustomBlockData data;

    public Chair(CustomBlockData data) {
        this.data = data;
    }

    @Override
    public CustomBlockData getCustomBlockData() {
        return data;
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
            Interaction interaction = getCustomBlockData().getPersistentDataContainer().get(INTERACTION_KEY, INTERACTION_DATA_TYPE);
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
