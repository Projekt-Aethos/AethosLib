package de.aethos.lib.blocks.type;

import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.blocks.CustomBlockFactory;
import de.aethos.lib.blocks.type.data.DisplayEntityData;
import de.aethos.lib.blocks.type.data.InteractionEntityData;
import de.aethos.lib.blocks.type.data.ItemData;
import de.aethos.lib.option.Option;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public record NoBlockType<C extends CustomBlock>(Class<C> block, CustomBlockFactory<C> factory,
                                                 JavaPlugin plugin, NamespacedKey key, ItemData itemData,
                                                 InteractionEntityData interactionEntityData,
                                                 DisplayEntityData displayEntityData) implements BlockType<C> {


    @Override
    public Option<InteractionEntityData> interaction() {
        return Option.some(interactionEntityData);
    }

    @Override
    public Option<DisplayEntityData> display() {
        return Option.some(displayEntityData);
    }

    @Override
    public Material vanillaDisplayMaterial() {
        return Material.AIR;
    }
}
