package de.aethos.lib.level;

import com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import de.aethos.lib.AethosLib;
import de.aethos.lib.data.AethosDataType;
import de.aethos.lib.level.interfaces.LevelledHolder;
import de.aethos.lib.level.interfaces.ProgressableLevelled;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static de.aethos.lib.level.LevelPointSpawner.XP_OBJECT_KEY;
import static de.aethos.lib.level.LevelPointSpawner.checkXpObject;

public class LevelPointListener implements Listener {
    private final JavaPlugin plugin;

    public LevelPointListener(final JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMerge(final ExperienceOrbMergeEvent event) {
        if (checkXpObject(event.getMergeSource()) || checkXpObject(event.getMergeTarget())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onXpCollect(final PlayerPickupExperienceEvent event) {
        final ExperienceOrb experienceOrb = event.getExperienceOrb();
        if (!checkXpObject(experienceOrb)) {
            return;
        }

        final PersistentDataContainer container = experienceOrb.getPersistentDataContainer();
        if (!container.has(XP_OBJECT_KEY, PersistentDataType.TAG_CONTAINER)) {
            return;
        }
        final PersistentDataContainer xpContainer = container.get(XP_OBJECT_KEY, PersistentDataType.TAG_CONTAINER);
        assert xpContainer != null;

        final LevelledHolder<UUID> levelClassHolder = AethosLib.getPlugin(AethosLib.class).getLevelApi()
                .getLevelClassHolder(event.getPlayer().getUniqueId());
        for (final NamespacedKey key : xpContainer.getKeys()) {
            final Double value = xpContainer.get(key, PersistentDataType.DOUBLE);
            if (value != null && levelClassHolder.get(key, true) instanceof ProgressableLevelled levelled) {
                levelled.addProgress(value);
            }
        }

        experienceOrb.setExperience(0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLoad(final EntitiesLoadEvent event) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTaskAsync -> {
            final Map<Display, UUID> displayList = new HashMap<>();
            EntityScheduler scheduler = null;
            for (final Entity entity : event.getEntities()) {
                if (entity instanceof Display display) {
                    final PersistentDataContainer container = display.getPersistentDataContainer();
                    if (container.has(XP_OBJECT_KEY, AethosDataType.UUID)) {
                        final UUID uuid = container.get(XP_OBJECT_KEY, AethosDataType.UUID);
                        assert uuid != null;
                        displayList.put(display, uuid);
                        if (scheduler == null) {
                            scheduler = display.getScheduler();
                        }
                    }
                }
            }

            if (scheduler != null) {
                scheduler.runDelayed(plugin, scheduledEntityTask -> displayList.forEach((display, uuid) -> {
                    if (display.getWorld().getEntity(uuid) instanceof ExperienceOrb experienceOrb) {
                        new LevelPointSpawner.DisplayKiller(experienceOrb, display).start();
                    } else {
                        display.remove();
                    }
                }), null, 1);
            }
        });
    }
}
