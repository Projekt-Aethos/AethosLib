package de.aethos.lib.inventory.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class ProtectedView extends View {
    public ProtectedView(@NotNull Menu menu, Player player) {
        super(menu, player);
    }
}