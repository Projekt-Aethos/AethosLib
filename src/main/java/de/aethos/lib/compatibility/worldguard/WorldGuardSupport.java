package de.aethos.lib.compatibility.worldguard;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public sealed interface WorldGuardSupport permits InactiveWorldGuardSupport, ExistingWorldGuardSupport {
    boolean isRegionFlagBlocked(@NotNull Player player, @NotNull Location location, @NotNull StateFlag flag);

    /**
     * @return true if PVP is blocked
     */
    boolean isPVPBlocked(@NotNull Player player, @NotNull Location location);

    /**
     * Checks if a {@link FlagKey} is explicit set to false
     *
     * @param location the {@link Location} to check for
     * @param player   the {@link Player} to check for the flag
     * @param flagKey  the {@link FlagKey} to check if it is forbidden
     * @return false if {@link WorldGuardPlugin} is disabled
     */
    boolean blockedByFlag(@NotNull Location location, @NotNull Player player, @NotNull FlagKey flagKey);

    /**
     * Use {@link #blockedByFlag(Location, Player, FlagKey)} if you want to check with a player
     *
     * @param location the {@link Location} to check
     * @param flagKey  the {@link FlagKey} to check
     * @return true if {@link WorldGuardPlugin} is disabled
     */
    boolean allowedByFlag(@NotNull Location location, @NotNull FlagKey flagKey);

    boolean isEnabled();
}
