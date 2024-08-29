package de.aethos.lib.blocks.type;

import com.google.common.base.Preconditions;
import de.aethos.lib.AethosLib;
import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.blocks.CustomBlockFactory;
import de.aethos.lib.blocks.type.data.DisplayEntityData;
import de.aethos.lib.blocks.type.data.InteractionEntityData;
import de.aethos.lib.blocks.type.data.ItemData;
import de.aethos.lib.data.AethosDataType;
import de.aethos.lib.option.Option;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public interface BlockType<C extends CustomBlock> {


    default void register() {
        Register.register(this);
    }

    Class<C> block();

    /*default ImmutablePair<BlockVector, BlockVector> dimensions() {
        return new ImmutablePair<>(new BlockVector(), new BlockVector());
    }*/

    CustomBlockFactory<C> factory();

    JavaPlugin plugin();

    NamespacedKey key();

    Option<InteractionEntityData> interaction();

    Option<DisplayEntityData> display();

    ItemData itemData();

    default Material breakMaterial() {
        return Material.DIRT;
    }

    Material vanillaDisplayMaterial();


    enum Rotation {
        ONE(1),
        FOUR(4),
        EIGHT(8),
        SIXTEEN(16),
        THIRTY_TWO(32);

        private final float high;

        private final float low;

        Rotation(final int possibleRotations) {
            this.high = 360f / possibleRotations;
            this.low = high / 2;
        }

        public float arrange(final float rotation) {
            final int direction = (int) (((rotation + low + 360) % 360) / high);
            return (direction * high + 180) % 360;
        }
    }

    final class Register {
        private static final Map<NamespacedKey, BlockType<? extends CustomBlock>> BLOCK_TYPE_MAP = new ConcurrentHashMap<>();


        public static Collection<BlockType<? extends CustomBlock>> values() {
            return BLOCK_TYPE_MAP.values();
        }

        public static Option<BlockType<CustomBlock>> from(ItemStack stack) {
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                return Option.none();
            }
            NamespacedKey key = Objects.requireNonNull(stack.getItemMeta().getPersistentDataContainer().get(BlockType.Key.TYPE_KEY, AethosDataType.NAMESPACED_KEY));
            return Option.of(from(key));
        }


        public static BlockType<CustomBlock> from(NamespacedKey key) {
            return (BlockType<CustomBlock>) BLOCK_TYPE_MAP.get(key);
        }

        public static Set<BlockType<? extends CustomBlock>> getCustomBlocks() {
            return new HashSet<>(BLOCK_TYPE_MAP.values());
        }

        public static void register(BlockType<? extends CustomBlock> type) {
            Preconditions.checkArgument(type.vanillaDisplayMaterial().isBlock());
            type.plugin().getLogger().info("Registering block type " + type);
            BLOCK_TYPE_MAP.put(type.key(), type);
        }
    }


    final class Key {
        public static final NamespacedKey TYPE_KEY = new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "type");
    }

}
