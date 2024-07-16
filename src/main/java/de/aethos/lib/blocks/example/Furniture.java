package de.aethos.lib.blocks.example;

import de.aethos.lib.AethosLib;
import de.aethos.lib.blocks.BlockType;
import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.blocks.CustomBlockFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

@BlockType(value = "Furniture", plugin = AethosLib.class, factory = Furniture.FurnitureFactory.class)
public class Furniture implements CustomBlock {


    private final Block block;
    private final NamespacedKey key;
    private final PersistentDataContainer container;

    protected Furniture(Block block, NamespacedKey key, PersistentDataContainer container) {
        this.block = block;
        this.key = key;
        this.container = container;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return container;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public void onCreate(BlockPlaceEvent event) {

    }

    @Override
    public void onInteract(PlayerInteractEvent event) {

    }

    @Override
    public void onSave(ChunkUnloadEvent event) {

    }

    @Override
    public void onBreak(BlockBreakEvent event) {

    }


    public static class FurnitureFactory implements CustomBlockFactory<Furniture> {

        public FurnitureFactory() {

        }

        @Override
        public Furniture construct(Block block, NamespacedKey key, PersistentDataContainer container) {
            return new Furniture(block, key, container);
        }
    }
}
