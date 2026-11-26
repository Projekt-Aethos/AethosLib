package de.aethos.lib.xp;

import java.util.Locale;

public enum XPType {
    VANILLA, FIGHTING, CRAFTING;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static XPType byId(String id) {
        if (id == null) {
            return null;
        }
        for (XPType type : values()) {
            if (type.id().equalsIgnoreCase(id.trim())) {
                return type;
            }
        }
        return null;
    }
}
