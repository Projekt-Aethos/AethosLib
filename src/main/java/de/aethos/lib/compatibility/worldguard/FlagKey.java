package de.aethos.lib.compatibility.worldguard;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import de.aethos.lib.AethosLib;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public interface FlagKey {
    Map<FlagKey, StateFlag> STATE_FLAGS = new HashMap<>();

    /**
     * Registers this {@link FlagKey} in the {@link WorldGuardPlugin}
     *
     * @param plugin to associate the flag with
     */
    default void register(@NotNull JavaPlugin plugin) {
        if (!AethosLib.getInstance().isWorldGuardSupportEnabled()) {
            return;
        }
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        // State flags
        String flagName = (plugin.getName() + "-" + this.getName()).toLowerCase(Locale.ROOT).replace('_', '-');
        try {
            StateFlag stateFlag = new StateFlag(flagName, this.getDefault());
            registry.register(stateFlag);
            STATE_FLAGS.put(this, stateFlag);
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get(flagName);
            if (existing instanceof StateFlag) {
                STATE_FLAGS.put(this, (StateFlag) existing);
            } else {
                AethosLib.getInstance().getLogger().log(Level.WARNING, "Konnte folgende Flagge nicht registrieren: " + flagName, e);
            }
        }

    }

    boolean getDefault();

    String getName();
}
