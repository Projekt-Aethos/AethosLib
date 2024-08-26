package de.aethos.lib.level;

import org.bukkit.NamespacedKey;

import java.io.Serial;

public class NoFactoryException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 6303748422662901957L;

    private final NamespacedKey key;

    public NoFactoryException(final NamespacedKey key) {
        super("No factory registered with key " + key);
        this.key = key;
    }

    public NamespacedKey getKey() {
        return key;
    }
}
