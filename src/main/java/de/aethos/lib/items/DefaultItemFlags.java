package de.aethos.lib.items;

import de.aethos.lib.AethosLib;
import de.aethos.lib.data.AethosDataType;
import org.bukkit.NamespacedKey;

/**
 * Default {@link ItemFlag}s.
 */
public enum DefaultItemFlags implements ItemFlag {
    /**
     * Prevents the drop from the player's inventory.
     * It combines {@link #NO_PLAYER_DROP_ALIVE} and {@link #NO_PLAYER_DROP_DEATH}.
     */
    NO_PLAYER_DROP,
    /**
     * Prevents the drop from the player's inventory when he is alive.
     */
    NO_PLAYER_DROP_ALIVE,
    /**
     * Prevents the drop from the player's inventory when he dies.
     */
    NO_PLAYER_DROP_DEATH,
    /**
     * Prevents the pickup.
     *
     * @see #ALLOW_PICKUP for exceptions
     */
    NO_PICKUP,
    /**
     * Allows the pickup of an item even when the {@link #NO_PICKUP} flag is set.
     * The UUID of the allowed players are stored in the PDC with
     * a {@link AethosDataType#UUID UUID} (or a {@link AethosDataType#LIST List} of it).
     */
    ALLOW_PICKUP,
    /**
     * Prevents the moving in inventories.
     */
    NO_MOVE,
    /**
     * Combines {@link #NO_CRAFT} and {@link #NO_SMITH}.
     */
    NO_MODIFICATION,
    /**
     * Prevents the usage in vanilla crafting.
     */
    NO_CRAFT,
    /**
     * Prevents the use in anvils, smiths and grindstones, includes {@link #NO_RENAME}.
     */
    NO_SMITH,
    /**
     * Prevents to set/change a custom name in the anvil.
     */
    NO_RENAME,
    /**
     * Prevents the usage as "first" item in an anvil, smith or grindstone.
     */
    NO_SMITH_BASE,
    /**
     * Prevents the usage as "second" item in an anvil, smith or grindstone.
     * Prevents also usage as template in the smith.
     */
    NO_SMITH_ADDITION,
    /**
     * Prevents interaction with the world using this item.
     */
    NO_INTERACTION,
    /**
     * Prevents the placing in the world.
     */
    NO_PLACE;

    private final NamespacedKey key = AethosLib.getKey(name());

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
