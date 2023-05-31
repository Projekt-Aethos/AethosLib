package de.ethos.ethoslib.database.hibernate;

import java.util.UUID;

public abstract class Entity {
    UUID uuid;

    public UUID getUniqueId() {
        return uuid;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }
}
