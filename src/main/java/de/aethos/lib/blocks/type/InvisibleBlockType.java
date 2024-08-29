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
import org.jetbrains.annotations.Nullable;

public record InvisibleBlockType<C extends CustomBlock>(Class<C> block, CustomBlockFactory<C> factory,
                                                        JavaPlugin plugin, NamespacedKey key, ItemData itemData,
                                                        @Nullable InteractionEntityData interactionEntityData,
                                                        DisplayEntityData displayEntityData,
                                                        Material vanillaDisplayMaterial) implements BlockType<C> {
    @Override
    public Option<InteractionEntityData> interaction() {
        return Option.of(interactionEntityData);
    }

    @Override
    public Option<DisplayEntityData> display() {
        return Option.some(displayEntityData);
    }

}
