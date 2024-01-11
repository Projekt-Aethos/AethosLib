package de.aethos.lib.compatibility.worldguard;

import com.sk89q.worldguard.protection.flags.StateFlag;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InactiveWorldGuardSupport implements WorldGuardSupport {
    @Override
    public boolean isRegionFlagBlocked(@NotNull Player player, @NotNull Location location, @NotNull StateFlag flag) {
        return false;
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
