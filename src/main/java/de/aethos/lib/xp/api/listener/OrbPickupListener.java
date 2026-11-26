package de.aethos.lib.xp.api.listener;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import de.aethos.lib.xp.OrbService;
import de.aethos.lib.xp.XPType;
import de.aethos.lib.xp.api.XPManager;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class OrbPickupListener implements Listener {

    private final OrbService orbs;
    private final XPManager manager;

    public OrbPickupListener(OrbService orbs, XPManager manager) {
        this.orbs = orbs;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(PlayerPickupExperienceEvent event) {
        ExperienceOrb orb = event.getExperienceOrb();
        orbs.adoptVanilla(orb);
        XPType type = orbs.typeOf(orb);
        int amount = orbs.amountOf(orb);
        orbs.removeDisplay(orb);
        if (type == XPType.VANILLA) {
            orb.setExperience(amount);
            return;
        }
        orb.setExperience(0);
        manager.givePoints(event.getPlayer(), type, amount);
    }
}
