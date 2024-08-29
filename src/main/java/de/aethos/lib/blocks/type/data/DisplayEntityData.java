package de.aethos.lib.blocks.type.data;

import de.aethos.lib.blocks.type.BlockType;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public record DisplayEntityData(Material item, int cmd, BlockType.Rotation rotations, Vector offset, float height,
                                float width, Consumer<ItemDisplay> consumer) {

    public static Consumer<ItemDisplay> NOTHING = interaction -> {
    };

    public static DisplayEntityData display(Material item, int cmd, BlockType.Rotation rotation) {
        return new DisplayEntityData(item, cmd, rotation, new Vector(), 1, 1, entity -> {
        });
    }
}
