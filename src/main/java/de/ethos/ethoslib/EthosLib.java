package de.ethos.ethoslib;

import de.ethos.ethoslib.GUI.GUIListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class EthosLib extends JavaPlugin {
    private static EthosLib INSTANCE;
    public static String chatPrefix;
    public static boolean isDebugEnabled;

    @Override
    public void onEnable() {
        INSTANCE = this;
        // Plugin startup logic
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new GUIListener(),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        isDebugEnabled = getConfig().getBoolean("debug", false);

        chatPrefix = getConfig().getString("chatPrefix", null);
    }


    //////////          Getter          ////////////
    public static EthosLib getINSTANCE() {return INSTANCE;}

}
