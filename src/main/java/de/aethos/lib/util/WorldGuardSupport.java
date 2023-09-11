package de.aethos.lib.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.aethos.lib.AethosLib;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class WorldGuardSupport {
    private RegionContainer container;

    public WorldGuardSupport() {
    }

    public static boolean isRegionFlagBlocked(@NotNull Player player, @NotNull Location location, @NotNull StateFlag flag) {
        if (!AethosLib.isWorldGuardEnabled) {
            return false;
        }

        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        return set.queryValue(lp, flag) == StateFlag.State.DENY;
    }

    /**
     * @return true if PVP is blocked
     */
    public static boolean isPVPBlocked(@NotNull Player player, @NotNull Location location) {
        if (!AethosLib.isWorldGuardEnabled) {
            return false;
        }
        return isRegionFlagBlocked(player, location, Flags.PVP);
    }

    public void loadRegions() {
        try {
            container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            AethosLib.isWorldGuardEnabled = true;
            Helper.log("WorldGuard-Unterstützung aktiviert!");
        } catch (Exception e) {
            AethosLib.isWorldGuardEnabled = false;
            Helper.log(Level.WARNING, "Fehler beim laden, WorldGuard-Unterstützung deaktiviert!");
        }
    }

    /**
     * Checks if a {@link FlagKey} is explicit set to false
     *
     * @param location the {@link Location} to check for
     * @param player   the {@link Player} to check for the flag
     * @param flagKey  the {@link FlagKey} to check if it is forbidden
     * @return false if {@link WorldGuardPlugin} is disabled
     */
    public boolean blockedByFlag(@NotNull Location location, @NotNull Player player, @NotNull FlagKey flagKey) {
        if (!AethosLib.isWorldGuardEnabled) {
            return false;
        }

        StateFlag flag = FlagKey.stateFlags.get(flagKey);
        if (flag == null) {
            return false;
        }

        World world = location.getWorld();
        if (world == null) {
            return false;
        }

        RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (regions == null) {
            return false;
        }
        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        StateFlag.State state = set.queryState(localPlayer, flag);
        return state == StateFlag.State.DENY;
    }

    /**
     * Use {@link #blockedByFlag(Location, Player, FlagKey)} if you want to check with a player
     *
     * @param location the {@link Location} to check
     * @param flagKey  the {@link FlagKey} to check
     * @return true if {@link WorldGuardPlugin} is disabled
     */
    public boolean allowedByFlag(@NotNull Location location, @NotNull FlagKey flagKey) {
        if (!AethosLib.isWorldGuardEnabled) {
            return true;
        }

        StateFlag flag = FlagKey.stateFlags.get(flagKey);
        if (flag == null) {
            return false;
        }

        return container.createQuery().testState(BukkitAdapter.adapt(location), null, flag);
    }

    public interface FlagKey {
        Map<FlagKey, StateFlag> stateFlags = new HashMap<>();

        /**
         * Registers this {@link FlagKey} in the {@link WorldGuardPlugin}
         *
         * @param plugin to associate the flag with
         */
        default void register(@NotNull JavaPlugin plugin) {
            if (!AethosLib.isWorldGuardEnabled) {
                return;
            }
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            // State flags
            String flagName = (plugin.getName() + "-" + this.getName()).toLowerCase(Locale.ROOT).replace('_', '-');
            try {
                StateFlag stateFlag = new StateFlag(flagName, this.getDefault());
                registry.register(stateFlag);
                stateFlags.put(this, stateFlag);
            } catch (FlagConflictException e) {
                Flag<?> existing = registry.get(flagName);
                if (existing instanceof StateFlag) {
                    stateFlags.put(this, (StateFlag) existing);
                } else {
                    AethosLib.getInstance().getLogger().log(Level.WARNING, "Konnte folgende Flagge nicht registrieren: " + flagName, e);
                }
            }

        }

        boolean getDefault();

        String getName();
    }
}