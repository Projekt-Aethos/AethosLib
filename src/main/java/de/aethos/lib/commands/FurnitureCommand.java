package de.aethos.lib.commands;

import de.aethos.lib.blocks.CustomBlocks;
import de.aethos.lib.blocks.example.Furniture;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FurnitureCommand extends Command {
    public FurnitureCommand(@NotNull String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player player && player.isOp()) {

            for (Method method : CustomBlocks.class.getMethods()) {
                if (method.getName().equals(args[0])) {
                    Parameter[] para = method.getParameters();
                    if (para[0].getType().equals(Block.class)) {
                        if (para.length > 1 && para[1].getType().equals(Class.class)) {
                            try {
                                player.sendMessage(String.valueOf(method.invoke(null, player.getLocation().getBlock(), Furniture.class)));
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        } else if (para.length > 1 && para[1].getType().equals(Set.class)) {
                            try {
                                player.sendMessage(String.valueOf(method.invoke(null, player.getLocation().getBlock(), Set.of(Furniture.class))));
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        }
                        try {
                            player.sendMessage(method.invoke(null, player.getLocation().getBlock()).toString());
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (para[0].getType().equals(Chunk.class)) {
                        if (para.length > 1 && para[1].getType().equals(Set.class)) {
                            try {
                                player.sendMessage(String.valueOf(method.invoke(null, player.getLocation().getChunk(), Set.of(Furniture.class))));
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                            return true;
                        }
                        try {
                            player.sendMessage(String.valueOf(method.invoke(null, player.getLocation().getChunk())));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        ArrayList<String> list = new ArrayList<>();
        for (Method method : CustomBlocks.class.getMethods()) {
            list.add(method.getName());
        }
        return list;
    }
}
