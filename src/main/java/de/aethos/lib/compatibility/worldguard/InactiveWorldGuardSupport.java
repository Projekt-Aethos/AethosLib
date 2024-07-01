package de.aethos.lib.compatibility.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class InactiveWorldGuardSupport implements WorldGuardSupport {

    public InactiveWorldGuardSupport(Plugin plugin) {
        plugin.getLogger().info("WorldGuardSupport inactive");
    }

    @Override
    public boolean isPVPBlocked(@NotNull Player player, @NotNull Location location) {
        return false;
    }

    @Override
    public boolean blockedByFlag(@NotNull Location location, @NotNull Player player, @NotNull FlagKey flagKey) {
        return false;
    }

    @Override
    public boolean allowedByFlag(@NotNull Location location, @NotNull FlagKey flagKey) {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
