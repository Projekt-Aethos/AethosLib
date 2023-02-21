package de.ethos.ethoslib.GUI;

import de.ethos.ethoslib.EthosLib;
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
import java.util.UUID;

public abstract class GUIItem extends ItemStack {

    private static final HashMap<UUID, GUIItem> map = new HashMap<>();
    private static final NamespacedKey UUIDKEY = new NamespacedKey(EthosLib.getINSTANCE(),"UUID");

    private final UUID uuid;
    public GUIItem(Material material, Component displayName, Component... lore){
        super(material);
        this.uuid = UUID.randomUUID();
        map.put(uuid, this);
        ItemMeta meta = this.getItemMeta();
        meta.getPersistentDataContainer().set(UUIDKEY, PersistentDataType.STRING, this.uuid.toString());
        meta.displayName(displayName);
        meta.lore(List.of(lore));
        this.setItemMeta(meta);
    }

    public static @Nullable GUIItem getItem(UUID uuid) {
        return map.get(uuid);
    }
    public static @Nullable GUIItem getItem(ItemStack item){
        return getItem(getUUID(item));
    }

    public @Nullable UUID getUUID(){
        return uuid;
    }

    public static UUID getUUID(ItemStack item){
       return UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(UUIDKEY,PersistentDataType.STRING));
    }

    public abstract void onKlick(@NotNull InventoryClickEvent event);


}


