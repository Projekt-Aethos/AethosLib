package de.aethos.lib.blocks.type.data;


import de.aethos.lib.blocks.type.BlockType;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public record InteractionEntityData(BlockType.Rotation rotations, Vector offset, float height,
                                    float width, boolean response, Consumer<Interaction> consumer) {
    public static Consumer<Interaction> NOTHING = interaction -> {
    };

}
