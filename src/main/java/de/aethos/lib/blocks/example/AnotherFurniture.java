package de.aethos.lib.blocks.example;

import de.aethos.lib.AethosLib;
import de.aethos.lib.blocks.BaseBlock;
import de.aethos.lib.blocks.BlockType;
import de.aethos.lib.blocks.CustomBlockFactory;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;

@BlockType(value = "AFurniture", plugin = AethosLib.class, factory = AnotherFurniture.AnotherFurnitureFactory.class)
public class AnotherFurniture extends BaseBlock {
 
    protected AnotherFurniture(Block block, NamespacedKey key, PersistentDataContainer container) {
        super(block, key, container);
    }

    public static class AnotherFurnitureFactory implements CustomBlockFactory<AnotherFurniture> {


        public AnotherFurnitureFactory() {

        }

        @Override
        public AnotherFurniture construct(Block block, NamespacedKey key, PersistentDataContainer container) {
            return new AnotherFurniture(block, key, container);
        }
    }
}
