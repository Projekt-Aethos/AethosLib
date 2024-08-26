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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public final class ExistingWorldGuardSupport implements WorldGuardSupport {
    @Nullable
    private RegionContainer container;

    public ExistingWorldGuardSupport(final Plugin plugin) {
        plugin.getLogger().info("WorldGuardSupport active");
    }

    @Override
    public boolean isPVPBlocked(final Player player, final Location location) {
        final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(player);
        final RegionQuery query = getContainer().createQuery();
        final ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));
        return set.queryValue(lp, Flags.PVP) == StateFlag.State.DENY;
    }

    @Override
    public boolean blockedByFlag(final Location location, final Player player, final FlagKey flagKey) {
        final StateFlag flag = FlagKey.STATE_FLAGS.get(flagKey);
        if (flag == null) {
            return false;
        }

        final World world = location.getWorld();
        if (world == null) {
            return false;
        }

        final RegionManager regions = getContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if (regions == null) {
            return false;
        }
        final ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
        final LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        final StateFlag.State state = set.queryState(localPlayer, flag);
        return state == StateFlag.State.DENY;
    }

    @Override
    public boolean allowedByFlag(final Location location, final FlagKey flagKey) {
        final StateFlag flag = FlagKey.STATE_FLAGS.get(flagKey);
        if (flag == null) {
            return false;
        }

        return getContainer().createQuery().testState(BukkitAdapter.adapt(location), null, flag);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private RegionContainer getContainer() {
        if (container == null) {
            container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        }
        return container;
    }
}
