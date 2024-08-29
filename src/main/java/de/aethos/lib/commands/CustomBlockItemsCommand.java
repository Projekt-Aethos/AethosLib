package de.aethos.lib.commands;

import de.aethos.lib.blocks.CustomBlock;
import de.aethos.lib.blocks.type.BlockType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomBlockItemsCommand extends Command {
    public CustomBlockItemsCommand() {
        super("cbitems");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (sender instanceof Player player) {
            for (BlockType<? extends CustomBlock> type : BlockType.Register.values()) {
                player.getInventory().addItem(type.itemData().createItem());
            }
            return true;
        }
        return false;
    }
}
