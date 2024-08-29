package de.aethos.lib.blocks;

import com.google.common.base.Preconditions;
import de.aethos.lib.AethosLib;
import de.aethos.lib.blocks.event.CustomBlockCanBuildEvent;
import de.aethos.lib.blocks.type.BlockType;
import de.aethos.lib.blocks.type.data.DisplayEntityData;
import de.aethos.lib.data.AethosDataType;
import de.aethos.lib.option.Option;
import de.aethos.lib.option.Some;
import de.aethos.lib.result.Result;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public interface CustomBlock {
    NamespacedKey DISPLAY_KEY = new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "display");
    NamespacedKey INTERACTION_KEY = new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "interaction");
    PersistentDataType<byte[], ItemDisplay> DISPLAY_DATA_TYPE = new AethosDataType.EntityReferenceDataType<>();
    PersistentDataType<byte[], Interaction> INTERACTION_DATA_TYPE = new AethosDataType.EntityReferenceDataType<>();
    NamespacedKey BLOCK_KEY = new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "block");

    static <T extends CustomBlock> Option<T> from(Block block, BlockType<T> type) {
        return data(block).map(data -> type.factory().construct(type, data));
    }

    private static Option<CustomBlockData> data(Block block) {
        return container(block).map(container -> new CustomBlockData(block, Key.generate(block), container));
    }

    private static Option<PersistentDataContainer> container(Block block) {
        final Chunk chunk = block.getChunk();
        final NamespacedKey key = Key.generate(block);
        final PersistentDataContainer container = chunk.getPersistentDataContainer().get(key, PersistentDataType.TAG_CONTAINER);
        return Option.of(container);
    }

    static boolean exists(Block block) {
        final Chunk chunk = block.getChunk();
        final NamespacedKey key = Key.generate(block);
        return chunk.getPersistentDataContainer().has(key, PersistentDataType.TAG_CONTAINER);
    }

    static void remove(CustomBlock custom) {
        Block block = custom.getCustomBlockData().block();
        NamespacedKey key = custom.getCustomBlockData().key();
        block.getChunk().getPersistentDataContainer().remove(key);
        block.setType(Material.AIR);
    }

    static <T extends CustomBlock> T create(Block block, BlockType<T> type) {
        Preconditions.checkArgument(!CustomBlock.exists(block), "Cant create a CustomBlock, because some CustomBlock already exists at " + Key.generate(block).getKey());
        final Chunk chunk = block.getChunk();
        final NamespacedKey key = Key.generate(block);
        final PersistentDataContainer container = chunk.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
        container.set(BlockType.Key.TYPE_KEY, AethosDataType.NAMESPACED_KEY, type.key());
        chunk.getPersistentDataContainer().set(key, PersistentDataType.TAG_CONTAINER, container);
        return type.factory().create(type, new CustomBlockData(block, key, container));
    }

    static <T extends CustomBlock> Result<T, Exception> place(Block block, BlockType<T> type, PlayerInteractEvent event) {
        if (CustomBlock.exists(block)) {
            return Result.err(new IllegalStateException("Cant create a CustomBlock, because some CustomBlock already exists at " + Key.generate(block).getKey()));
        }

        final Chunk chunk = block.getChunk();
        final NamespacedKey key = Key.generate(block);
        final PersistentDataContainer container = chunk.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
        final CustomBlockData data = new CustomBlockData(block, key, container);
        container.set(BlockType.Key.TYPE_KEY, AethosDataType.NAMESPACED_KEY, type.key());

        //TODO TEST

        final T custom = type.factory().place(type, block, data, event);

        //TODO Check if Events
        //final CustomBlockPlaceEvent simulated = new CustomBlockPlaceEvent(custom, block.getState(), event.getClickedBlock(), event.getItem(), event.getPlayer(), true, EquipmentSlot.HAND);
        final CustomBlockCanBuildEvent simulated = new CustomBlockCanBuildEvent(custom, event.getPlayer(), block.getBlockData(), true, EquipmentSlot.HAND);
        if (simulated.callEvent()) {
            chunk.getPersistentDataContainer().set(key, PersistentDataType.TAG_CONTAINER, container);
            return Result.ok(custom);
        }
        custom.onRemove();
        block.setType(Material.AIR);
        return Result.err(new IllegalStateException("Cant create a CustomBlock"));
    }

    static Option<? extends CustomBlock> get(Block block) {
        if (data(block) instanceof Some<CustomBlockData> some) {
            final CustomBlockData data = some.value();
            final NamespacedKey key = Objects.requireNonNull(data.container().get(BlockType.Key.TYPE_KEY, AethosDataType.NAMESPACED_KEY));
            final BlockType<CustomBlock> type = BlockType.Register.from(key);
            final CustomBlockFactory<CustomBlock> constructor = type.factory();
            return Option.some(constructor.construct(type, data));
        }
        return Option.none();
    }

    static Set<? extends CustomBlock> get(Chunk chunk) {
        return blocks(chunk)
                .stream()
                .map(CustomBlock::get)
                .flatMap(Option::stream)
                .collect(Collectors.toSet());
    }

    static void clear(Chunk chunk) {
        blocks(chunk)
                .stream()
                .map(CustomBlock::get)
                .flatMap(Option::stream)
                .forEach(CustomBlock::remove);
    }

    static Set<Block> blocks(Chunk chunk) {
        return chunk.getPersistentDataContainer()
                .getKeys()
                .stream()
                .filter(Key.KEY_REGEX_PREDICATE)
                .map(key -> Key.block(key, chunk))
                .collect(Collectors.toSet());
    }

    static Option<? extends CustomBlock> get(Entity entity) {
        final NamespacedKey key = entity.getPersistentDataContainer().get(BLOCK_KEY, AethosDataType.NAMESPACED_KEY);
        if (key != null) {
            Block block = Key.block(key, entity.getChunk());
            return get(block);
        }
        return Option.none();
    }

    CustomBlockData getCustomBlockData();

    void onPlace(PlayerInteractEvent event);

    default void onEntityInteract(PlayerInteractEntityEvent event) {

    }

    void onRemove();

    void onInteract(PlayerInteractEvent event);

    void onSave(ChunkUnloadEvent event);

    Collection<ItemStack> getDrops();

    default <T extends Entity> T spawn(Vector offset, Class<T> def, Consumer<? super T> consumer, NamespacedKey key) {
        final Location location = getCustomBlockData().block().getLocation().toCenterLocation().add(offset);
        final T entity = location.getWorld().spawn(location, def, consumer);
        getCustomBlockData().edit(pdc -> pdc.set(key, new AethosDataType.EntityReferenceDataType<>(), entity));
        entity.getPersistentDataContainer().set(BLOCK_KEY, AethosDataType.NAMESPACED_KEY, getCustomBlockData().key());
        return entity;
    }

    default Option<Interaction> spawnInteractionDisplay(float yaw) {
        final BlockType<?> type = this.getBlockType();

        return type.interaction().map(data -> spawn(data.offset(), Interaction.class, interact -> {
            interact.setRotation(data.rotations().arrange(yaw), 0);
            interact.setResponsive(data.response());
            interact.setInteractionHeight(data.height());
            interact.setInteractionWidth(data.width());
        }, INTERACTION_KEY));
    }

    default ItemDisplay spawnItemDisplay(float yaw) {
        final BlockType<?> type = this.getBlockType();
        final DisplayEntityData dis = type.display().orElseThrow(IllegalStateException::new);
        return spawn(dis.offset(), ItemDisplay.class, display -> {
            display.setItemStack(type.itemData().createItem());
            display.setRotation(dis.rotations().arrange(yaw), 0);
            display.setDisplayHeight(dis.height());
            display.setDisplayWidth(dis.width());
            dis.consumer().accept(display);
        }, DISPLAY_KEY);
    }

    default void despawn(NamespacedKey key) {
        Entity entity = getCustomBlockData().getPersistentDataContainer().get(key, new AethosDataType.EntityReferenceDataType<>());
        if (entity != null) {
            entity.remove();
        }
    }

    default void despawnItemDisplay() {
        despawn(DISPLAY_KEY);
    }

    default void despawnInteraction() {
        despawn(INTERACTION_KEY);
    }


    default float getDestroySpeed(ItemStack stack) {
        return getBlockType().breakMaterial().createBlockData().getDestroySpeed(stack, true);
    }

    default Collection<ItemStack> getDrops(ItemStack tool) {
        return getDrops();
    }

    default Collection<ItemStack> getDrops(ItemStack tool, Entity entity) {
        return getDrops();
    }

    BlockType<? extends CustomBlock> getBlockType();


    final class Key {

        private static final Pattern KEY_REGEX = Pattern.compile("^x(\\d+)y(-?\\d+)z(\\d+)$");
        private static final Predicate<NamespacedKey> KEY_REGEX_PREDICATE = input -> KEY_REGEX.matcher(input.getKey()).matches();


        public static NamespacedKey generate(Block block) {
            int x = block.getX() & 15;
            int z = block.getZ() & 15;
            return AethosLib.getKey("x" + x + "y" + block.getY() + "z" + z);
        }


        public static Block block(NamespacedKey key, Chunk chunk) {
            final Matcher matcher = KEY_REGEX.matcher(key.getKey());
            Preconditions.checkArgument(matcher.matches());
            final int x = Integer.parseInt(matcher.group(1));
            final int y = Integer.parseInt(matcher.group(2));
            final int z = Integer.parseInt(matcher.group(3));
            return chunk.getBlock(x, y, z);
        }
    }

}
