package de.aethos.lib.blocks.example;

import de.aethos.lib.AethosLib;
import de.aethos.lib.blocks.AbstractCustomBlock;
import de.aethos.lib.blocks.CustomBlockData;
import de.aethos.lib.blocks.CustomBlockFactory;
import de.aethos.lib.blocks.type.BlockType;
import de.aethos.lib.blocks.type.data.DisplayEntityData;
import de.aethos.lib.blocks.type.data.InteractionEntityData;
import de.aethos.lib.blocks.type.data.ItemData;
import de.aethos.lib.option.Option;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;

public class Table extends AbstractCustomBlock {

    public Table(BlockType<Table> type, CustomBlockData data) {
        super(type, data);
    }

    @Override
    public void onPlace(PlayerInteractEvent event) {

    }

    @Override
    public void onRemove() {

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


    public static class TableType implements BlockType<Table> {
        @Override
        public Class<Table> block() {
            return Table.class;
        }

        @Override
        public CustomBlockFactory<Table> factory() {
            return Table::new;
        }

        @Override
        public JavaPlugin plugin() {
            return AethosLib.getPlugin(AethosLib.class);
        }

        @Override
        public NamespacedKey key() {
            return AethosLib.getKey("table");
        }

        @Override
        public Option<InteractionEntityData> interaction() {
            return Option.none();
        }

        @Override
        public Option<DisplayEntityData> display() {
            return Option.none();
        }

        @Override
        public ItemData itemData() {
            return new ItemData(Material.STICK, 1, Component.text("Tisch"), key(), meta -> {
            });
        }

        @Override
        public Material vanillaDisplayMaterial() {
            return Material.BARRIER;
        }

    }
}
