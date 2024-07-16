package de.aethos.lib.blocks;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.event.platform.BlockInteractEvent;
import de.aethos.lib.AethosLib;
import de.aethos.lib.option.None;
import de.aethos.lib.option.Option;
import de.aethos.lib.option.Some;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public interface CustomBlock extends PersistentDataHolder {


    static <T extends CustomBlock> Option<T> from(Block block, Class<T> cl) {
        final Option<PersistentDataContainer> data = data(block);
        if (data instanceof Some<PersistentDataContainer> some) {
            final PersistentDataContainer container = some.value();
            final NamespacedKey key = Key.generate(block);
            final CustomBlockFactory<T> constructor = BlockType.Register.getFactory(cl);
            return Option.some(constructor.construct(block, key, container));
        }
        return Option.none();
    }

    static <T extends CustomBlock> T create(Block block, Class<T> t) {
        Preconditions.checkArgument(data(block) instanceof None<?>);
        final BlockType type = t.getAnnotation(BlockType.class);
        final CustomBlockFactory<T> constructor = BlockType.Register.getFactory(t);
        final Chunk chunk = block.getChunk();
        final NamespacedKey key = Key.generate(block);
        final PersistentDataContainer container = chunk.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();
        container.set(Key.TYPE_KEY, PersistentDataType.STRING, type.value());
        chunk.getPersistentDataContainer().set(key, PersistentDataType.TAG_CONTAINER, container);
        return constructor.create(block, key, container);
    }

    static Option<PersistentDataContainer> data(Block block) {
        final Chunk chunk = block.getChunk();
        final NamespacedKey key = Key.generate(block);
        final PersistentDataContainer container = chunk.getPersistentDataContainer().get(key, PersistentDataType.TAG_CONTAINER);
        return Option.of(container);
    }

    static Option<? extends CustomBlock> get(Block block) {
        final Option<PersistentDataContainer> data = data(block);
        if (data(block) instanceof Some<PersistentDataContainer> some) {
            final PersistentDataContainer container = some.value();
            final NamespacedKey key = Key.generate(block);
            final String type = container.get(Key.TYPE_KEY, PersistentDataType.STRING);
            final CustomBlockFactory<?> constructor = BlockType.Register.STRING_FACTORY_MAP.getOrDefault(type, UndefinedType::new);
            return Option.some(constructor.construct(block, key, container));
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

    Block getBlock();

    @Override
    @NotNull PersistentDataContainer getPersistentDataContainer();

    NamespacedKey getKey();

    default void remove() {
        getBlock().getChunk().getPersistentDataContainer().remove(getKey());
    }

    void onCreate(BlockPlaceEvent event);

    void onInteract(BlockInteractEvent event);

    void onSave(ChunkUnloadEvent event);

    void onBreak(BlockBreakEvent event);

    default Collection<ItemStack> getDrops() {
        return getBlock().getDrops();
    }

    default Collection<ItemStack> getDrops(ItemStack tool) {
        return getBlock().getDrops(tool);
    }

    default Collection<ItemStack> getDrops(ItemStack tool, Entity entity) {
        return getBlock().getDrops(tool, entity);
    }

    interface Key {

        NamespacedKey TYPE_KEY = new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "type");
        Pattern KEY_REGEX = Pattern.compile("^x(\\d+)y(-?\\d+)z(\\d+)$");
        Predicate<NamespacedKey> KEY_REGEX_PREDICATE = input -> KEY_REGEX.matcher(input.getKey()).matches();

        static NamespacedKey generate(Block block) {
            int x = block.getX() & 15;
            int z = block.getZ() & 15;
            return AethosLib.getKey("x" + x + "y" + block.getY() + "z" + z);
        }


        static Block block(NamespacedKey key, Chunk chunk) {
            final Matcher matcher = KEY_REGEX.matcher(key.getKey());
            Preconditions.checkArgument(matcher.matches());
            final int x = Integer.parseInt(matcher.group(1));
            final int y = Integer.parseInt(matcher.group(2));
            final int z = Integer.parseInt(matcher.group(3));
            return chunk.getBlock(x, y, z);
        }
    }

}
