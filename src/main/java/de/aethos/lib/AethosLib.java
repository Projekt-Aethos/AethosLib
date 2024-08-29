package de.aethos.lib;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.aethos.lib.blocks.event.listener.CustomBlockBreakListener;
import de.aethos.lib.blocks.event.listener.CustomBlockInteractListener;
import de.aethos.lib.blocks.event.listener.CustomBlockPlaceListener;
import de.aethos.lib.blocks.example.Muelleimer;
import de.aethos.lib.blocks.example.SmallDiamond;
import de.aethos.lib.blocks.type.BlockOverwriteType;
import de.aethos.lib.blocks.type.BlockType;
import de.aethos.lib.blocks.type.NoBlockType;
import de.aethos.lib.blocks.type.data.DisplayEntityData;
import de.aethos.lib.blocks.type.data.InteractionEntityData;
import de.aethos.lib.blocks.type.data.ItemData;
import de.aethos.lib.commands.CustomBlockItemsCommand;
import de.aethos.lib.compatibility.worldguard.ExistingWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.InactiveWorldGuardSupport;
import de.aethos.lib.compatibility.worldguard.WorldGuardSupport;
import de.aethos.lib.data.database.connector.Connector;
import de.aethos.lib.data.database.connector.DefaultPluginConnector;
import de.aethos.lib.level.LevelApi;
import de.aethos.lib.level.LevelPointListener;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.janboerman.guilib.api.GuiListener;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public final class AethosLib extends JavaPlugin {
    private final LevelApi levelApi;
    private final WorldGuardSupport worldGuardSupport;

    public AethosLib() {
        worldGuardSupport = loadWorldGuardSupport();
        levelApi = new LevelApi(this);
    }

    public static NamespacedKey getKey(String key) {
        return new NamespacedKey(AethosLib.getPlugin(AethosLib.class), key);
    }

    @Override
    public void onLoad() {
        getLogger().info("✓ AethosLib successfully loaded");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @Override
    public void onEnable() {


        new BlockOverwriteType<>(Muelleimer.class, Muelleimer::new, this, new NamespacedKey(this, "garbage_can"), new ItemData(Material.BARREL, 1, Component.text("Mülleimer"), new NamespacedKey(this, "garbage_can"), meta -> {
        }), Material.BARREL, Material.BARREL).register();


        NamespacedKey key = new NamespacedKey(this, "small_decoration");
        ItemData item = new ItemData(Material.DIAMOND, 1, Component.text("Kleine Decoration"), key, meta -> {
        });
        DisplayEntityData display = new DisplayEntityData(Material.DIAMOND, 1, BlockType.Rotation.ONE, new Vector(), 0.5f, 0.5f, DisplayEntityData.NOTHING);
        InteractionEntityData interaction = new InteractionEntityData(BlockType.Rotation.ONE, new Vector(), 0.5f, 0.5f, false, InteractionEntityData.NOTHING);
        new NoBlockType<>(SmallDiamond.class, SmallDiamond::new, this, key, item, interaction, display).register();

        saveDefaultConfig();
        getServer().getCommandMap().register("aehtoslib", new CustomBlockItemsCommand());
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(CustomBlockPlaceListener.INSTANCE, this);
        pm.registerEvents(CustomBlockBreakListener.INSTANCE, this);
        pm.registerEvents(CustomBlockInteractListener.INSTANCE, this);

        pm.registerEvents(GuiListener.getInstance(), this);
        pm.registerEvents(new LevelPointListener(this), this);

        getLogger().info("✓ AethosLib successfully activated");
    }

    private WorldGuardSupport loadWorldGuardSupport() {
        Logger logger = getLogger();
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                String version = WorldGuardPlugin.inst().getDescription().getVersion();
                if (version.contains("7.0")) {
                    return new ExistingWorldGuardSupport(this);
                } else {
                    logger.info("WorldGuard Version 7.0.X erforderlich, vorhanden ist " + version);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Fehler beim Laden, WorldGuard-Unterstützung deaktiviert!");
            }
        }
        return new InactiveWorldGuardSupport(this);
    }

    @Contract("_ -> new")
    public @NotNull Connector getConnector(@NotNull JavaPlugin plugin) {
        return new DefaultPluginConnector(plugin);
    }

    public @NotNull WorldGuardSupport getWorldGuardSupport() {
        return worldGuardSupport;
    }

    @Contract(" -> new")
    public @NotNull Connector getConnector() {
        return new DefaultPluginConnector(this);
    }

    public @NotNull LevelApi getLevelApi() {
        return levelApi;
    }


    public boolean isWorldGuardSupportEnabled() {
        return worldGuardSupport.isEnabled();
    }
}
