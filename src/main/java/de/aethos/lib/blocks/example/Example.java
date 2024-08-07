package de.aethos.lib.blocks.example;

import de.aethos.lib.AethosLib;
import de.aethos.lib.blocks.BaseBlock;
import de.aethos.lib.blocks.BlockType;
import de.aethos.lib.blocks.CustomBlockData;
import de.aethos.lib.blocks.CustomBlockFactory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

@BlockType(value = "Example", plugin = AethosLib.class, factory = Example.ExampleFactory.class)
public class Example extends BaseBlock {


    protected Example(CustomBlockData data) {
        super(data);
    }

    @Override
    public Collection<ItemStack> getDrops() {
        return List.of();
    }


    public static class ExampleFactory implements CustomBlockFactory<Example> {

        public ExampleFactory() {

        }


        @Override
        public Example construct(CustomBlockData data) {
            return new Example(data);
        }
    }
}
