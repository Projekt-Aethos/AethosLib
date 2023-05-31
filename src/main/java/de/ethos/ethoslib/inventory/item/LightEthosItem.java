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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LightEthosItem extends ItemStack {
    public static final FunctionalEventHandler<InventoryClickEvent> CANCEL = e -> e.setCancelled(true);
    public static final FunctionalEventHandler<InventoryClickEvent> EXIT = e -> e.getView().close();
    public static final FunctionalEventHandler<InventoryClickEvent> ERROR = e -> {
        e.setCancelled(true);
        e.getWhoClicked().sendMessage(Component.text("Error, das hätte nicht passieren sollen", NamedTextColor.RED));
    };
    private static final Map<UUID, FunctionalEventHandler<InventoryClickEvent>> MAP = new HashMap<>();
    private static final NamespacedKey UUID_KEY = new NamespacedKey(EthosLib.getINSTANCE(), "UUID");

    public LightEthosItem(@Nullable FunctionalEventHandler<InventoryClickEvent> handler, @NotNull Material material, @NotNull Component displayName, @NotNull Component... lore) {
        this(handler, material, displayName, List.of(lore));
    }

    private LightEthosItem(@Nullable FunctionalEventHandler<InventoryClickEvent> handler, @NotNull Material material, @NotNull Component displayName, @NotNull List<Component> lore) {
        super(material);
        if (handler == null) {
            handler = CANCEL;
        }
        final UUID uuid = UUID.randomUUID();
        MAP.put(uuid, handler);

        if (this.hasItemMeta()) {
            ItemMeta meta = this.getItemMeta();
            meta.getPersistentDataContainer().set(UUID_KEY, PersistentDataType.STRING, String.valueOf(uuid));
            meta.displayName(displayName);
            meta.lore(lore);
            this.setItemMeta(meta);
        }
    }

    /**
     * Will call the eventHandler if the clicked Item is associated with one
     *
     * @param event that the eventHandler accepts
     */
    public static void onKlick(@NotNull final InventoryClickEvent event) {
        if (event.getCurrentItem() == null) {
            return;
        }
        if (event.getCurrentItem().getItemMeta() == null) {
            return;
        }
        String string = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(UUID_KEY, PersistentDataType.STRING);
        if (string == null) {
            return;
        }
        UUID uuid = UUID.fromString(string);
        MAP.get(uuid).onEvent(event);
    }

}
