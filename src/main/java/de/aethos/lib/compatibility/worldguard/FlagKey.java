package de.aethos.lib.compatibility.worldguard;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.aethos.lib.AethosLib;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public interface FlagKey {
    /**
     * Registers this {@link FlagKey} in the {@link WorldGuardPlugin}.
     *
     * @param plugin to associate the flag with
     */
    default void register(final JavaPlugin plugin) {
        AethosLib.getPlugin(AethosLib.class).getWorldGuardSupport().register(this, plugin);
    }

    /**
     * Gets the default (unassigned) value of this flag.
     *
     * @return the value if not specified
     */
    boolean getDefault();

    /**
     * Gets the name of this flag to register.
     *
     * @return the name without special characters (expect '-' and '_')
     */
    String getName();
}
