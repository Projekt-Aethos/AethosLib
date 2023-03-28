package de.ethos.ethoslib.inventory.item;

import de.ethos.ethoslib.EthosLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.*;

public class LightEthosItem extends ItemStack{
    private static final HashMap<UUID, FunctionalEventHandler<InventoryClickEvent>> MAP = new HashMap<>();
    private static final NamespacedKey UUID_KEY = new NamespacedKey(EthosLib.getINSTANCE(), "UUID");
    public static final FunctionalEventHandler<InventoryClickEvent> CANCEL = e -> e.setCancelled(true);
    public static final FunctionalEventHandler<InventoryClickEvent> EXIT = e -> e.getView().close();
    public static final FunctionalEventHandler<InventoryClickEvent> ERROR = e -> {
        e.setCancelled(true);
        e.getWhoClicked().sendMessage(Component.text("Error, das hätte nicht passieren sollen", NamedTextColor.RED));
    };


    /**
     *      * Default Constructor
     */
    public LightEthosItem(@Nullable FunctionalEventHandler<InventoryClickEvent> handler, @NotNull Material material, @NotNull Component displayName, @NotNull Component... lore) {
        this(handler,material,displayName,List.of(lore));
    }


    private LightEthosItem(@Nullable FunctionalEventHandler<InventoryClickEvent> handler, @NotNull Material material, @NotNull Component displayName, @NotNull List<Component> lore) {
        super(material);
        if(handler == null){
            handler = CANCEL;
        }
        final UUID uuid = UUID.randomUUID();
        MAP.put(uuid, handler);
        UUIDtoItemStack(this,uuid);
        ItemMeta meta = this.getItemMeta();
        meta.displayName(displayName);
        meta.lore(lore);
        this.setItemMeta(meta);
    }


    private static @NotNull Optional<UUID> UUIDfromItemStack(@Nullable ItemStack item){
        if(item == null){
            return Optional.empty();
        }
        if(item.getItemMeta() == null){
            return Optional.empty();
        }
        final String uuidString = item.getItemMeta().getPersistentDataContainer().get(UUID_KEY, PersistentDataType.STRING);
        if(uuidString == null){
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(uuidString));
    }
    private static void UUIDtoItemStack(@NotNull ItemStack item,@NotNull UUID uuid){
        if(item.hasItemMeta()){
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_KEY, PersistentDataType.STRING, String.valueOf(uuid));
            item.setItemMeta(meta);
        }
    }

    /**
     * Will call the eventHandler if the clicked Item is associated with one
     * @param event that the eventHandler accepts
     */
    public static void onKlick(@NotNull final InventoryClickEvent event){
        EventHandlerof(event.getCurrentItem()).ifPresentOrElse(handler -> handler.onEvent(event), () -> CANCEL.onEvent(event));
    }

    /**
     * Will return an Optional of the eventHandler that is associated with the Item
     * @return Optional of the eventHandler
     */
    private static @NotNull Optional<FunctionalEventHandler<InventoryClickEvent>> EventHandlerof(@Nullable ItemStack item){
        return Optional.of(MAP.get(UUIDfromItemStack(item).get()));
    }

    /**
     * Converts an Item to a LightEthosItem
     * @param item that should be Converter
     * @param handler the eventHandler that should be associated with the Item
     */

    public static void convert(@NotNull ItemStack item,@NotNull FunctionalEventHandler<InventoryClickEvent> handler){
        UUIDfromItemStack(item).ifPresentOrElse(uuid -> MAP.put(uuid, handler),
            () -> {
                UUID uuid = UUID.randomUUID();
                UUIDtoItemStack(item,uuid);
                MAP.put(uuid, handler);
            }
        );
    }

}
