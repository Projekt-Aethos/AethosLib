package de.aethos.lib.items;

import de.aethos.lib.data.AethosDataType;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.*;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static de.aethos.lib.items.DefaultItemFlags.*;

/**
 * Handles interaction with {@link DefaultItemFlags}.
 */
public class DefaultItemFlagListener implements Listener {
    /**
     * Handles {@link DefaultItemFlags#NO_PLAYER_DROP_ALIVE}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDrop(final PlayerDropItemEvent event) {
        if (hasAFlag(event.getItemDrop().getItemStack(), NO_PLAYER_DROP, NO_PLAYER_DROP_ALIVE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_PLAYER_DROP_DEATH}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (event.getKeepInventory()) {
            return;
        }
        final List<ItemStack> itemsToKeep = event.getItemsToKeep();
        final Iterator<ItemStack> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            final ItemStack item = iterator.next();
            if (hasAFlag(item, NO_PLAYER_DROP, NO_PLAYER_DROP_DEATH)) {
                iterator.remove();
                itemsToKeep.add(item);
            }
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_PICKUP}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemPickup(final EntityPickupItemEvent event) {
        final ItemStack stack = event.getItem().getItemStack();
        if (!hasAFlag(stack, NO_PICKUP)) {
            return;
        }

        final PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
        final NamespacedKey allowPickupKey = ALLOW_PICKUP.getKey();
        if (!pdc.has(allowPickupKey)) {
            event.setCancelled(true);
            return;
        }

        if (pdc.has(allowPickupKey, AethosDataType.UUID)) {
            final UUID allowedUuid = pdc.get(allowPickupKey, AethosDataType.UUID);
            if (event.getEntity().getUniqueId().equals(allowedUuid)) {
                return;
            }
            event.setCancelled(true);
            return;
        }

        final ListPersistentDataType<byte[], UUID> listDataType = AethosDataType.LIST.listTypeFrom(AethosDataType.UUID);
        if (pdc.has(allowPickupKey, listDataType)) {
            final List<UUID> uuids = pdc.get(allowPickupKey, listDataType);
            assert uuids != null;
            if (uuids.contains(event.getEntity().getUniqueId())) {
                return;
            }
            event.setCancelled(true);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_MOVE} with inventory clicks.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(final InventoryClickEvent event) {
        if (hasAFlag(event.getCurrentItem(), NO_MOVE) || hasAFlag(event.getCursor(), NO_MOVE)
                || event.getClick() == ClickType.NUMBER_KEY
                && hasAFlag(event.getWhoClicked().getInventory().getItem(event.getHotbarButton()), NO_MOVE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_MOVE} with inventory item drags.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(final InventoryDragEvent event) {
        if (hasAFlag(event.getCursor(), NO_MOVE) || hasAFlag(event.getOldCursor(), NO_MOVE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_MOVE} with the off-hand swap key outside the inventory.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(final PlayerSwapHandItemsEvent event) {
        if (hasAFlag(event.getMainHandItem(), NO_MOVE) || hasAFlag(event.getOffHandItem(), NO_MOVE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_MOVE} with hoppers and similar.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(final InventoryMoveItemEvent event) {
        // Performance with hoppers?
        if (hasAFlag(event.getItem(), NO_MOVE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_CRAFT}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCraftPrepare(final PrepareItemCraftEvent event) {
        final CraftingInventory inventory = event.getInventory();
        for (@Nullable final ItemStack matrix : inventory.getMatrix()) {
            if (hasAFlag(matrix, NO_MODIFICATION, NO_CRAFT)) {
                inventory.setResult(null);
                return;
            }
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_SMITH} and {@link DefaultItemFlags#NO_RENAME} with anvils.
     */
    @EventHandler
    public void onAnvil(final PrepareAnvilEvent event) {
        final AnvilInventory inventory = event.getInventory();
        if (hasPreventSmithFlag(inventory.getFirstItem(), inventory.getSecondItem())) {
            event.setResult(null);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_SMITH} with smiths.
     */
    @EventHandler
    public void onSmith(final PrepareSmithingEvent event) {
        final SmithingInventory inventory = event.getInventory();
        if (hasPreventSmithFlag(inventory.getInputEquipment(), inventory.getInputEquipment())
                || hasPreventSmithFlag(null, inventory.getInputTemplate())) {
            event.setResult(null);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_SMITH} with grindstones.
     */
    @EventHandler
    public void onGrindstone(final PrepareGrindstoneEvent event) {
        final GrindstoneInventory inventory = event.getInventory();
        if (hasPreventSmithFlag(inventory.getUpperItem(), inventory.getLowerItem())) {
            event.setResult(null);
        }
    }

    private boolean hasPreventSmithFlag(@Nullable final ItemStack base, @Nullable final ItemStack addition) {
        return hasAFlag(base, NO_MODIFICATION, NO_SMITH, NO_SMITH_BASE)
                || hasAFlag(addition, NO_MODIFICATION, NO_SMITH, NO_SMITH_ADDITION);
    }

    /**
     * Handles {@link DefaultItemFlags#NO_PLACE}.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent event) {
        if (hasAFlag(event.getItemInHand(), NO_INTERACTION, NO_PLACE)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_INTERACTION} with generous interaction.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        if (hasAFlag(event.getItem(), NO_INTERACTION)) {
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_INTERACTION} as damage source.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LivingEntity living) {
            final EntityEquipment equipment = living.getEquipment();
            if (equipment != null && hasAFlag(equipment.getItemInMainHand(), NO_INTERACTION)) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_INTERACTION} with entities.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEntityEvent event) {
        if (hasAFlag(event.getPlayer().getEquipment().getItem(event.getHand()), NO_INTERACTION)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles {@link DefaultItemFlags#NO_INTERACTION} with item frames.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(final PlayerItemFrameChangeEvent event) {
        if (hasAFlag(event.getItemStack(), NO_INTERACTION)) {
            event.setCancelled(true);
        }
    }

    @Contract("null, _ -> false")
    private boolean hasAFlag(@Nullable final ItemStack itemStack, final ItemFlag... flags) {
        if (itemStack == null || itemStack.isEmpty() || !itemStack.hasItemMeta()) {
            return false;
        }

        final PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
        for (final ItemFlag flag : flags) {
            if (pdc.has(flag.getKey())) {
                return true;
            }
        }
        return false;
    }
}
