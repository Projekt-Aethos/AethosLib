package de.ethos.ethoslib.util;

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
import de.ethos.ethoslib.EthosLib;
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

    public static boolean isRegionFlagBlocked(@NotNull final Player player, @NotNull final Location location, @NotNull final StateFlag flag) {
        if (!EthosLib.isWorldGuardEnabled) {
            return false;
        }

        final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        final RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        final ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        return set.queryValue(lp, flag) == StateFlag.State.DENY;
    }

    /**
     * @return true if PVP is blocked
     */
    public static boolean isPVPBlocked(@NotNull final Player player, @NotNull final Location location) {
        if (!EthosLib.isWorldGuardEnabled) {
            return false;
        }
        return isRegionFlagBlocked(player, location, Flags.PVP);
    }

    public void loadRegions() {
        try {
            container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            EthosLib.isWorldGuardEnabled = true;
            Helper.log("WorldGuard-Unterstützung aktiviert!");
        } catch (final Exception e) {
            EthosLib.isWorldGuardEnabled = false;
            Helper.log(Level.WARNING, "Fehler beim laden, WorldGuard-Unterstützung deaktiviert!");
        }
    }

    public boolean blockedByFlag(@NotNull final Location location, @NotNull final Player player, @NotNull final FlagKey flagKey) {
        if (!EthosLib.isWorldGuardEnabled) {
            return false;
        }

        final StateFlag flag = FlagKey.stateFlags.get(flagKey);
        if (flag == null) {
            return false;
        }

        final World world = location.getWorld();
        if (world == null) {
            return false;
        }

        final RegionManager regions = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (regions == null) {
            return false;
        }
        final ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
        final LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        final StateFlag.State state = set.queryState(localPlayer, flag);
        return state == StateFlag.State.DENY;
    }

    public interface FlagKey {
        Map<FlagKey, StateFlag> stateFlags = new HashMap<>();

        /**
         * Registers a {@link Flag} in the {@link WorldGuardPlugin}
         *
         * @param plugin to associate the flag with
         */
        default void register(@NotNull final JavaPlugin plugin) {
            if (!EthosLib.isWorldGuardEnabled) {
                return;
            }
            final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            // State flags
            final String flagName = (plugin.getName() + "-" + this.getName()).toLowerCase(Locale.ROOT).replace('_', '-');
            try {
                final StateFlag stateFlag = new StateFlag(flagName, this.getDefault());
                registry.register(stateFlag);
                stateFlags.put(this, stateFlag);
            } catch (final FlagConflictException e) {
                final Flag<?> existing = registry.get(flagName);
                if (existing instanceof StateFlag) {
                    stateFlags.put(this, (StateFlag) existing);
                } else {
                    EthosLib.getINSTANCE().getLogger().log(Level.WARNING, "Konnte folgende Flagge nicht registrieren: " + flagName, e);
                }
            }

        }

        boolean getDefault();

        String getName();
    }
}