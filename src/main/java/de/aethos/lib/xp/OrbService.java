package de.aethos.lib.xp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public final class OrbService {

    private final NamespacedKey typeKey;
    private final NamespacedKey amountKey;
    private final Map<XPType, Integer> markers = new EnumMap<>(XPType.class);

    static final String DISPLAY_TAG = "aethosxp_display";

    private double mergeRadius = 1.5;
    private int maxAmount = 0;

    private boolean displayEnabled;
    private float displayScale = 0.35f;
    private float displayYOffset = -0.2f;
    private final Map<XPType, Material> displayItems = new EnumMap<>(XPType.class);
    private final Map<XPType, NamespacedKey> displayModels = new EnumMap<>(XPType.class);

    public OrbService(Plugin plugin) {
        this.typeKey = new NamespacedKey(plugin, "type");
        this.amountKey = new NamespacedKey(plugin, "amount");
    }

    public void configure(Map<XPType, Integer> markers, double mergeRadius, int maxAmount) {
        this.markers.clear();
        this.markers.putAll(markers);
        this.mergeRadius = mergeRadius;
        this.maxAmount = maxAmount;
    }

    public void configureDisplay(boolean enabled, float scale, float yOffset,
                                 Map<XPType, Material> items, Map<XPType, NamespacedKey> models) {
        this.displayEnabled = enabled;
        this.displayScale = scale;
        this.displayYOffset = yOffset;
        this.displayItems.clear();
        this.displayItems.putAll(items);
        this.displayModels.clear();
        this.displayModels.putAll(models);
    }

    ItemStack displayItem(XPType type, int amount) {
        ItemStack item = new ItemStack(displayItems.get(type));
        NamespacedKey model = displayModels.get(type);
        if (model == null) {
            model = new NamespacedKey("aethos", "item/xp/" + type.id() + "_" + OrbSprite.index(amount));
        }
        NamespacedKey finalModel = model;
        item.editMeta(meta -> meta.setItemModel(finalModel));
        return item;
    }

    private ItemDisplay displayOf(ExperienceOrb orb) {
        for (Entity passenger : orb.getPassengers()) {
            if (passenger instanceof ItemDisplay display && display.getScoreboardTags().contains(DISPLAY_TAG)) {
                return display;
            }
        }
        return null;
    }

    public void ensureDisplay(ExperienceOrb orb) {
        if (!displayEnabled || !orb.isValid()) {
            return;
        }
        XPType type = typeOf(orb);
        if (type == null || displayOf(orb) != null) {
            return;
        }
        ItemStack item = displayItem(type, amountOf(orb));
        ItemDisplay display = orb.getWorld().spawn(orb.getLocation(), ItemDisplay.class, d -> {
            d.setPersistent(false);
            d.addScoreboardTag(DISPLAY_TAG);
            d.setItemStack(item);
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GUI);
            d.setBillboard(Display.Billboard.CENTER);
            d.setBrightness(new Display.Brightness(15, 15));
            d.setTransformation(new Transformation(
                    new Vector3f(0f, displayYOffset, 0f), new AxisAngle4f(),
                    new Vector3f(displayScale, displayScale, displayScale), new AxisAngle4f()));
        });
        orb.addPassenger(display);
    }

    public void removeDisplay(ExperienceOrb orb) {
        for (Entity passenger : orb.getPassengers()) {
            if (passenger.getScoreboardTags().contains(DISPLAY_TAG)) {
                passenger.remove();
            }
        }
    }

    public void removeDisplays(Iterable<World> worlds, boolean onlyOrphans) {
        for (World world : worlds) {
            for (ItemDisplay display : world.getEntitiesByClass(ItemDisplay.class)) {
                if (display.getScoreboardTags().contains(DISPLAY_TAG)
                        && !(onlyOrphans && display.getVehicle() instanceof ExperienceOrb)) {
                    display.remove();
                }
            }
        }
    }

    public int marker(XPType type) {
        return markers.get(type);
    }

    public XPType typeOf(ExperienceOrb orb) {
        return XPType.byId(orb.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING));
    }

    public int amountOf(ExperienceOrb orb) {
        normalizeCount(orb);
        Integer amount = orb.getPersistentDataContainer().get(amountKey, PersistentDataType.INTEGER);
        return amount == null ? 0 : amount;
    }

    public void adoptVanilla(ExperienceOrb orb) {
        if (typeOf(orb) != null) {
            return;
        }
        long real = (long) orb.getExperience() * Math.max(1, orb.getCount());
        write(orb, XPType.VANILLA, clamp(real));
    }

    public void write(ExperienceOrb orb, XPType type, int amount) {
        PersistentDataContainer data = orb.getPersistentDataContainer();
        data.set(typeKey, PersistentDataType.STRING, type.id());
        data.set(amountKey, PersistentDataType.INTEGER, Math.max(0, amount));
        orb.setCount(1);
        orb.setExperience(marker(type));
    }

    public ExperienceOrb spawn(Location location, XPType type, int amount) {
        return location.getWorld().spawn(location, ExperienceOrb.class, orb -> write(orb, type, amount));
    }

    public boolean merge(ExperienceOrb target, ExperienceOrb source) {
        if (target.equals(source) || !target.isValid() || !source.isValid()) {
            return false;
        }
        XPType type = typeOf(target);
        if (type == null || type != typeOf(source)) {
            return false;
        }
        long sum = (long) amountOf(target) + amountOf(source);
        if (sum > Integer.MAX_VALUE || (maxAmount > 0 && sum > maxAmount)) {
            return false;
        }
        target.getPersistentDataContainer().set(amountKey, PersistentDataType.INTEGER, (int) sum);
        removeDisplay(source);
        source.remove();
        ItemDisplay display = displayOf(target);
        if (display != null) {
            display.setItemStack(displayItem(type, (int) sum));
        }
        return true;
    }

    public Map<XPType, Integer> mergeAll(Iterable<World> worlds, boolean mergeEnabled) {
        Map<XPType, Integer> counts = new EnumMap<>(XPType.class);
        Set<UUID> gone = new HashSet<>();
        Set<UUID> counted = new HashSet<>();
        for (World world : worlds) {
            for (ExperienceOrb orb : world.getEntitiesByClass(ExperienceOrb.class)) {
                if (gone.contains(orb.getUniqueId()) || !orb.isValid()) {
                    continue;
                }
                adoptVanilla(orb);
                XPType type = typeOf(orb);
                counted.add(orb.getUniqueId());
                counts.merge(type, 1, Integer::sum);
                ensureDisplay(orb);
                if (!mergeEnabled) {
                    continue;
                }
                for (Entity near : orb.getNearbyEntities(mergeRadius, mergeRadius, mergeRadius)) {
                    if (near instanceof ExperienceOrb other && !gone.contains(other.getUniqueId())) {
                        adoptVanilla(other);
                        if (merge(orb, other)) {
                            gone.add(other.getUniqueId());
                            if (!counted.add(other.getUniqueId())) {
                                counts.merge(type, -1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }
        removeDisplays(worlds, true);
        return counts;
    }

    private void normalizeCount(ExperienceOrb orb) {
        int count = orb.getCount();
        if (count <= 1) {
            return;
        }
        if (typeOf(orb) == XPType.VANILLA) {
            PersistentDataContainer data = orb.getPersistentDataContainer();
            Integer amount = data.get(amountKey, PersistentDataType.INTEGER);
            long sum = (amount == null ? 0 : amount) + (long) (count - 1) * orb.getExperience();
            data.set(amountKey, PersistentDataType.INTEGER, clamp(sum));
        }
        orb.setCount(1);
    }

    private static int clamp(long value) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, value));
    }
}
