package de.ethos.ethoslib.GUI;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProtectedView extends View{
    public ProtectedView(@NotNull Menu menu, Player player) {
        super(menu, player);
    }
}
