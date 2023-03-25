package de.ethos.ethoslib.testing.placeholder;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaceholderAPI {

    public static boolean isPresent(){
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public static boolean registerIfPresent(List<Placeholder> placeholderList){
        if(isPresent()){
            register(placeholderList);
            return true;
        }
        return false;
    }
    private static void register(List<Placeholder> placeholderList){
        for(Placeholder placeholder : placeholderList){
            if(placeholder.canRegister()){
                try{
                    placeholder.register();
                } catch (Exception exception){
                    error(placeholder,exception.getMessage());
                }
            } else{
                error(placeholder,"Cant be registered");
            }
        }
    }
    private static void error(Placeholder placeholder, String error){
        Logger logger = placeholder.getProviderPlugin().getLogger();
        logger.log(Level.WARNING,"ERROR: Placeholder " + placeholder.getIdentifier());
        logger.log(Level.WARNING,"ERROR: "+ error);
    }

    public static Component getTextComponent(Player player, String string){
        if(isPresent()){
            string = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player,string);
        }
        return Component.text(string);

    }
}
