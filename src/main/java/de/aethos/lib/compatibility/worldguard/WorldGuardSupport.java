package de.aethos.lib.compatibility.worldguard;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public sealed interface WorldGuardSupport permits InactiveWorldGuardSupport, ExistingWorldGuardSupport {
    /**
     * Registers a new {@link FlagKey}.
     *
     * @param flagKey to register
     * @param plugin  to associate the flag with
     */
    void register(FlagKey flagKey, JavaPlugin plugin);

    /**
     * Checks if PVP is explicit blocked.
     *
     * @param player   the player to check for
     * @param location where to check
     * @return true if PVP is blocked
     */
    boolean isPVPBlocked(Player player, Location location);

    /**
     * Checks if a {@link FlagKey} is explicit set to false.
     *
     * @param location the {@link Location} to check for
     * @param player   the {@link Player} to check for the flag
     * @param flagKey  the {@link FlagKey} to check if it is forbidden
     * @return false if {@link WorldGuardPlugin} is disabled
     */
    boolean blockedByFlag(Location location, Player player, FlagKey flagKey);

    /**
     * Use {@link #blockedByFlag(Location, Player, FlagKey)} if you want to check with a player.
     *
     * @param location the {@link Location} to check
     * @param flagKey  the {@link FlagKey} to check
     * @return true if {@link WorldGuardPlugin} is disabled
     */
    boolean allowedByFlag(Location location, FlagKey flagKey);

    boolean isEnabled();
}
