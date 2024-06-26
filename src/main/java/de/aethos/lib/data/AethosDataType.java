package de.aethos.lib.data;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

public interface AethosDataType<P, C> extends PersistentDataType<P, C> {
    PersistentDataType<byte[], UUID> UUID = new UUIDDataType();
    PersistentDataType<String, NamespacedKey> NAMESPACED_KEY = new NamespacedKeyDataType();

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
