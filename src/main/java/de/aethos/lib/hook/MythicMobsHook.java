package de.aethos.lib.hook;

import io.lumine.mythic.api.exceptions.InvalidMobTypeException;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public final class MythicMobsHook {

    public static final String PLUGIN = "MythicMobs";

    private MythicMobsHook() {
    }

    public static Collection<String> mobNames() {
        return List.copyOf(MythicBukkit.inst().getMobManager().getMobNames());
    }

    @Nullable
    public static String mobName(final String name) {
        for (final String candidate : mobNames()) {
            if (candidate.equalsIgnoreCase(name)) {
                return candidate;
            }
        }
        return null;
    }

    @Nullable
    public static Entity spawn(final String name, final Location location, final int level) {
        final String mob = mobName(name);
        if (mob == null) {
            return null;
        }
        try {
            return MythicBukkit.inst().getAPIHelper().spawnMythicMob(mob, location, level);
        } catch (final InvalidMobTypeException e) {
            return null;
        }
    }
}
