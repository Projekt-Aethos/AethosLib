package de.aethos.lib.xp.api.listener;

import de.aethos.lib.xp.OrbService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.plugin.Plugin;

public class OrbSpawnListener implements Listener {

    private final Plugin plugin;
    private final OrbService orbs;

    public OrbSpawnListener(Plugin plugin, OrbService orbs) {
        this.plugin = plugin;
        this.orbs = orbs;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof ExperienceOrb orb) {
            orbs.adoptVanilla(orb);
            orb.getScheduler().run(plugin, task -> orbs.ensureDisplay(orb), null);
        }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof ExperienceOrb orb) {
                orbs.adoptVanilla(orb);
            }
        }
    }
}
