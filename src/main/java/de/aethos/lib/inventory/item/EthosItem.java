package de.aethos.lib.inventory.item;

import de.aethos.lib.AethosLib;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Deprecated
public abstract class EthosItem extends ItemStack {

    private static final Map<UUID, EthosItem> MAP = new HashMap<>();
    private static final NamespacedKey UUID_KEY = new NamespacedKey(AethosLib.getInstance(), "UUID");

    private final UUID uuid;

    public EthosItem(Material material, Component displayName, Component... lore) {
        super(material);
        this.uuid = UUID.randomUUID();
        MAP.put(uuid, this);
        //TODO temporäre Items  (beim schließen des Inventars löschen)
        ItemMeta meta = this.getItemMeta();
        meta.getPersistentDataContainer().set(UUID_KEY, PersistentDataType.STRING, this.uuid.toString());
        meta.displayName(displayName);
        meta.lore(List.of(lore));
        this.setItemMeta(meta);
    }

    public static @Nullable EthosItem getItem(@Nullable UUID uuid) {
        return MAP.get(uuid);
    }

    public static @Nullable EthosItem getItem(@Nullable ItemStack item) {
        return getItem(getUUID(item));
    }

    public static @Nullable UUID getUUID(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        String uuidString = item.getItemMeta().getPersistentDataContainer().get(UUID_KEY, PersistentDataType.STRING);
        if (uuidString != null) {
            return UUID.fromString(uuidString);
        }
        return null;
    }

    public @Nullable UUID getUUID() {
        return uuid;
    }

    public abstract void onKlick(@NotNull InventoryClickEvent event);

}
