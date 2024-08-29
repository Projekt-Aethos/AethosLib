package de.aethos.lib.data;

import de.aethos.lib.AethosLib;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

public interface AethosDataType<P, C> extends PersistentDataType<P, C> {
    PersistentDataType<byte[], UUID> UUID = new UUIDDataType();
    PersistentDataType<String, NamespacedKey> NAMESPACED_KEY = new NamespacedKeyDataType();
    PersistentDataType<PersistentDataContainer, Location> LOCATION = new LocationDataType();
    PersistentDataType<byte[], World> WORLD = new WorldDataType();
    PersistentDataType<PersistentDataContainer, ItemStack> ITEM = new ItemDataType();

    class EntityReferenceDataType<T extends Entity> implements PersistentDataType<byte[], T> {


        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<T> getComplexType() {
            return (Class<T>) Entity.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(@NotNull Entity complex, @NotNull PersistentDataAdapterContext context) {
            return UUID.toPrimitive(complex.getUniqueId(), context);
        }

        @Override
        public @NotNull T fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            UUID uuid = UUID.fromPrimitive(primitive, context);
            return (T) Bukkit.getEntity(uuid);
        }
    }


    class UUIDDataType implements PersistentDataType<byte[], UUID> {
        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
            buffer.putLong(complex.getMostSignificantBits());
            buffer.putLong(complex.getLeastSignificantBits());
            return buffer.array();
        }

        @Override
        public @NotNull UUID fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            ByteBuffer buffer = ByteBuffer.wrap(primitive);
            long most = buffer.getLong();
            long least = buffer.getLong();
            return new UUID(most, least);
        }
    }

    class WorldDataType implements PersistentDataType<byte[], World> {

        @Override
        public @NotNull Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public @NotNull Class<World> getComplexType() {
            return World.class;
        }

        @Override
        public byte @NotNull [] toPrimitive(@NotNull World complex, @NotNull PersistentDataAdapterContext context) {
            return UUID.toPrimitive(complex.getUID(), context);
        }

        @Override
        public @NotNull World fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            return Objects.requireNonNull(Bukkit.getWorld(UUID.fromPrimitive(primitive, context)));
        }
    }

    class EnumDataType<E extends Enum<E>> implements PersistentDataType<String, E> {

        Class<E> e;

        public EnumDataType(Class<E> e) {
            this.e = e;
        }

        @Override
        public @NotNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public @NotNull Class<E> getComplexType() {
            return e;
        }

        @Override
        public @NotNull String toPrimitive(@NotNull E complex, @NotNull PersistentDataAdapterContext context) {
            return complex.name();
        }

        @Override
        public @NotNull E fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
            for (E constants : e.getEnumConstants()) {
                if (constants.name().equalsIgnoreCase(primitive)) {
                    return constants;
                }
            }
            throw new IllegalStateException("Could not find enum constant " + primitive);
        }
    }

    class ItemMetaDataType implements PersistentDataType<String, ItemMeta> {
        @Override
        public @NotNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public @NotNull Class<ItemMeta> getComplexType() {
            return ItemMeta.class;
        }

        @Override
        public @NotNull String toPrimitive(@NotNull ItemMeta complex, @NotNull PersistentDataAdapterContext context) {
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.set("meta", complex.getAsString());
            return configuration.saveToString();
        }

        @Override
        public @NotNull ItemMeta fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
            YamlConfiguration configuration = new YamlConfiguration();
            try {
                configuration.loadFromString(primitive);
            } catch (InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
            return configuration.getObject("meta", ItemMeta.class);
        }


    }

    class ItemDataType implements PersistentDataType<PersistentDataContainer, ItemStack> {
        private final static NamespacedKey TYPE_KEY = new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "type");
        private final static NamespacedKey META_KEY = new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "meta");

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<ItemStack> getComplexType() {
            return ItemStack.class;

        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull ItemStack complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(TYPE_KEY, new EnumDataType<>(Material.class), complex.getType());
            container.set(META_KEY, new ItemMetaDataType(), complex.getItemMeta());

            return container;
        }

        @Override
        public @NotNull ItemStack fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            Material material = primitive.get(TYPE_KEY, new EnumDataType<>(Material.class));
            ItemMeta meta = primitive.get(META_KEY, new ItemMetaDataType());
            ItemStack stack = new ItemStack(material);
            stack.setItemMeta(meta);
            return stack;
        }
    }

    class LocationDataType implements PersistentDataType<PersistentDataContainer, Location> {

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<Location> getComplexType() {
            return Location.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull Location complex, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "world"), AethosDataType.WORLD, complex.getWorld());
            container.set(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "pitch"), AethosDataType.FLOAT, complex.getPitch());
            container.set(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "yaw"), AethosDataType.FLOAT, complex.getYaw());
            container.set(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "x"), AethosDataType.DOUBLE, complex.getX());
            container.set(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "y"), AethosDataType.DOUBLE, complex.getY());
            container.set(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "z"), AethosDataType.DOUBLE, complex.getZ());
            return container;
        }

        @Override
        public @NotNull Location fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
            World world = primitive.get(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "world"), AethosDataType.WORLD);
            float pitch = primitive.get(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "pitch"), AethosDataType.FLOAT);
            float yaw = primitive.get(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "yaw"), AethosDataType.FLOAT);
            double x = primitive.get(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "x"), AethosDataType.DOUBLE);
            double y = primitive.get(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "y"), AethosDataType.DOUBLE);
            double z = primitive.get(new NamespacedKey(AethosLib.getPlugin(AethosLib.class), "z"), AethosDataType.DOUBLE);
            return new Location(world, x, y, z, pitch, yaw);
        }


    }

    class NamespacedKeyDataType implements PersistentDataType<String, NamespacedKey> {
        @Override
        public @NotNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public @NotNull Class<NamespacedKey> getComplexType() {
            return NamespacedKey.class;
        }

        @Override
        public @NotNull String toPrimitive(@NotNull NamespacedKey complex, @NotNull PersistentDataAdapterContext context) {
            return complex.asMinimalString();
        }

        @Override
        public @NotNull NamespacedKey fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
            return Objects.requireNonNull(NamespacedKey.fromString(primitive));
        }
    }
}
