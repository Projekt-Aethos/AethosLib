package de.aethos.lib.compatibility.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExistingWorldGuardSupport implements WorldGuardSupport {
    private final RegionContainer container;

    public ExistingWorldGuardSupport() {
        container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    @Override
    public boolean isRegionFlagBlocked(@NotNull Player player, @NotNull Location location, @NotNull StateFlag flag) {
        LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        return set.queryValue(lp, flag) == StateFlag.State.DENY;
    }

    @Override
    public boolean isPVPBlocked(@NotNull Player player, @NotNull Location location) {
        return isRegionFlagBlocked(player, location, Flags.PVP);
    }

    @Override
    public boolean blockedByFlag(@NotNull Location location, @NotNull Player player, @NotNull FlagKey flagKey) {
        StateFlag flag = FlagKey.STATE_FLAGS.get(flagKey);
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

    @Override
    public boolean allowedByFlag(@NotNull Location location, @NotNull FlagKey flagKey) {
        StateFlag flag = FlagKey.STATE_FLAGS.get(flagKey);
        if (flag == null) {
            return false;
        }

        return container.createQuery().testState(BukkitAdapter.adapt(location), null, flag);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}