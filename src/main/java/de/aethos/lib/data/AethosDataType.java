package de.aethos.lib.data;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;

public interface AethosDataType<P, C> extends PersistentDataType<P, C> {
    PersistentDataType<byte[], UUID> UUID = new UUIDDataType();
    PersistentDataType<String, NamespacedKey> NAMESPACED_KEY = new NamespacedKeyDataType();

    class UUIDDataType implements PersistentDataType<byte[], UUID> {
        @Override
        public Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public byte[] toPrimitive(final UUID complex, final PersistentDataAdapterContext context) {
            final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
            buffer.putLong(complex.getMostSignificantBits());
            buffer.putLong(complex.getLeastSignificantBits());
            return buffer.array();
        }

        @Override
        public UUID fromPrimitive(final byte[] primitive, final PersistentDataAdapterContext context) {
            final ByteBuffer buffer = ByteBuffer.wrap(primitive);
            final long most = buffer.getLong();
            final long least = buffer.getLong();
            return new UUID(most, least);
        }
    }

    class NamespacedKeyDataType implements PersistentDataType<String, NamespacedKey> {
        @Override
        public Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public Class<NamespacedKey> getComplexType() {
            return NamespacedKey.class;
        }

        @Override
        public String toPrimitive(final NamespacedKey complex, final PersistentDataAdapterContext context) {
            return complex.asMinimalString();
        }

        @Override
        public NamespacedKey fromPrimitive(final String primitive, final PersistentDataAdapterContext context) {
            return Objects.requireNonNull(NamespacedKey.fromString(primitive));
        }
    }
}
