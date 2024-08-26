package de.aethos.lib.compatibility.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class InactiveWorldGuardSupport implements WorldGuardSupport {
    public InactiveWorldGuardSupport(final Plugin plugin) {
        plugin.getLogger().info("WorldGuardSupport inactive");
    }

    @Override
    public boolean isPVPBlocked(final Player player, final Location location) {
        return false;
    }

    @Override
    public boolean blockedByFlag(final Location location, final Player player, final FlagKey flagKey) {
        return false;
    }

    @Override
    public boolean allowedByFlag(final Location location, final FlagKey flagKey) {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
