package de.ethos.ethoslib.GUI;

import de.ethos.ethoslib.EthosLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GUIListener implements Listener {
    public enum typ {
        GUI(new NamespacedKey(EthosLib.getINSTANCE(), "protected_gui"));

        public final NamespacedKey namespacedKey;
        typ(NamespacedKey namespacedKey) {
            this.namespacedKey = namespacedKey;
        }
    }

    public static @Nullable ItemStack hasKeyString(@Nullable ItemStack stack, NamespacedKey key) {
        if (stack == null || stack.getType().isAir())
            return null;
        if (stack.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING))
            return stack;
        else return null;
    }

    public static @Nullable String getProtectedGUIItemValue(@NotNull ItemMeta meta) {
        return meta.getPersistentDataContainer().get(typ.GUI.namespacedKey, PersistentDataType.STRING);
    }

}
