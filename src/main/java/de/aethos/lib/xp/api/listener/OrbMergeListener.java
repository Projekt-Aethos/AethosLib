package de.aethos.lib.xp.api.listener;

import com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent;
import de.aethos.lib.xp.OrbService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.function.BooleanSupplier;

public class OrbMergeListener implements Listener {

    private final OrbService orbs;
    private final BooleanSupplier mergeEnabled;

    public OrbMergeListener(OrbService orbs, BooleanSupplier mergeEnabled) {
        this.orbs = orbs;
        this.mergeEnabled = mergeEnabled;
    }

    @EventHandler(ignoreCancelled = true)
    public void onVanillaMerge(ExperienceOrbMergeEvent event) {
        event.setCancelled(true);
        if (mergeEnabled.getAsBoolean()) {
            orbs.adoptVanilla(event.getMergeTarget());
            orbs.adoptVanilla(event.getMergeSource());
            orbs.merge(event.getMergeTarget(), event.getMergeSource());
        }
    }
}
