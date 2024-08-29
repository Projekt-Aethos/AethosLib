package de.aethos.lib;

import org.bukkit.Material;

public enum Wood {
    OAK(Material.OAK_LOG),
    BIRCH(Material.BIRCH_LOG),
    SPRUCE(Material.SPRUCE_LOG),
    MAHOGANY(Material.JUNGLE_LOG),
    ACACIA(Material.ACACIA_LOG),
    WALNUT(Material.DARK_OAK_LOG),
    MANGROVE(Material.MANGROVE_LOG),
    CHERRY(Material.CHERRY_LOG),
    PINE(Material.CRIMSON_STEM),
    MAPLE(Material.WARPED_STEM);


    private final Material log;

    Wood(Material log) {
        this.log = log;
    }

    public Material getLog() {
        return log;
    }
}
