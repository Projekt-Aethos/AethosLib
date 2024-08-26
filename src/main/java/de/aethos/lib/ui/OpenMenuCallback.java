package de.aethos.lib.ui;

import de.aethos.lib.AethosLib;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import xyz.janboerman.guilib.api.menu.MenuHolder;

public class OpenMenuCallback implements ClickCallback<Audience> {
    protected final MenuHolder<?> menuHolder;

    public OpenMenuCallback(final MenuHolder<?> menuHolder) {
        this.menuHolder = menuHolder;
    }

    @Override
    public void accept(final Audience audience) {
        if (audience instanceof HumanEntity entity) {
            final Inventory inv = menuHolder.getInventory();
            entity.openInventory(inv);
            return;
        }
        AethosLib.getPlugin(AethosLib.class).getLogger()
                .info("Failed to open Menu for Audience because Audience is not of Type HumanEntity");
    }
}
