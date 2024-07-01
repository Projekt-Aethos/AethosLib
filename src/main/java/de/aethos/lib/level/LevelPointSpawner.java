package de.aethos.lib.level;

import de.aethos.lib.AethosLib;
import de.aethos.lib.data.AethosDataType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * The class to spawn experience with.
 */
public final class LevelPointSpawner {
    public static final NamespacedKey XP_OBJECT_KEY = new NamespacedKey(getPlugin(), "xp_object_key");

    private LevelPointSpawner() {

    }

    public static boolean checkXpObject(@NotNull ExperienceOrb mergeSource) {
        PersistentDataContainer container = mergeSource.getPersistentDataContainer();
        return !container.isEmpty() && container.has(XP_OBJECT_KEY);
    }

    /**
     * Spawns an experience orb for an experience class represented.
     *
     * @param location to spawn the orb at
     * @param key      of the class
     * @param amount   of the orb
     * @return the orb
     */
    public static @NotNull ExperienceOrb spawn(@NotNull Location location, @NotNull NamespacedKey key, double amount) {
        ExperienceOrb entity = spawn(location, Map.of(key, amount));
        assert entity != null;
        return entity;
    }

    /**
     * Spawns an experience orb with class values.
     *
     * @param location to spawn the orb at
     * @param xpValues values of the orb
     * @return null if xpValues is empty
     */
    public static @Nullable ExperienceOrb spawn(@NotNull Location location, @NotNull Map<NamespacedKey, Double> xpValues) {
        if (xpValues.isEmpty()) {
            return null;
        }

        ExperienceOrb experienceOrb = location.getWorld().spawn(location, ExperienceOrb.class, orb -> {
            PersistentDataContainer container = orb.getPersistentDataContainer();
            PersistentDataContainer xpContainer = container.getAdapterContext().newPersistentDataContainer();
            for (Map.Entry<NamespacedKey, Double> entry : xpValues.entrySet()) {
                xpContainer.set(entry.getKey(), PersistentDataType.DOUBLE, entry.getValue());
            }
            container.set(XP_OBJECT_KEY, PersistentDataType.TAG_CONTAINER, xpContainer);
        });
        ItemDisplay display = location.getWorld().spawn(location, ItemDisplay.class);
        display.getPersistentDataContainer().set(XP_OBJECT_KEY, AethosDataType.UUID, experienceOrb.getUniqueId());
        display.setItemStack(new ItemStack(Material.EXPERIENCE_BOTTLE));
        display.setBillboard(Display.Billboard.CENTER);
        // TODO custom model data & more data
        new DisplayKiller(experienceOrb, display).start();
        experienceOrb.addPassenger(display);
        return experienceOrb;
    }

    private static @NotNull JavaPlugin getPlugin() {
        return AethosLib.getPlugin(AethosLib.class);
    }

    /**
     * Removes the display if the orb is no longer valid.
     */
    public static class DisplayKiller extends BukkitRunnable {
        private final ExperienceOrb experienceOrb;

        private final Display display;

        public DisplayKiller(@NotNull ExperienceOrb experienceOrb, @NotNull Display display) {
            this.experienceOrb = experienceOrb;
            this.display = display;
        }

        @Override
        public void run() {
            if (!experienceOrb.isValid()) {
                display.remove();
            }
        }

        public void start() {
            display.getScheduler().runAtFixedRate(getPlugin(), scheduledTask -> run(), null, 1, 1);
        }
    }

}

