package de.aethos.lib.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiListener;
import xyz.janboerman.guilib.api.menu.MenuHolder;

@SuppressWarnings("unused")
public class ComponentMenuHolder<P extends Plugin> extends MenuHolder<P> {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public ComponentMenuHolder(final P plugin, final InventoryType type, final String title) {
        this(plugin, type, MINI_MESSAGE.deserialize(title));
    }

    public ComponentMenuHolder(final P plugin, final InventoryType type, final Component title) {
        super(plugin, type, deserialize(title));
    }

    public ComponentMenuHolder(final GuiListener guiListener, final P plugin, final InventoryType type, final String title) {
        this(guiListener, plugin, type, MINI_MESSAGE.deserialize(title));
    }

    public ComponentMenuHolder(final GuiListener guiListener, final P plugin, final InventoryType type, final Component title) {
        super(guiListener, plugin, type, deserialize(title));
    }

    public ComponentMenuHolder(final P plugin, final int size, final String title) {
        this(plugin, size, MINI_MESSAGE.deserialize(title));
    }

    public ComponentMenuHolder(final P plugin, final int size, final Component title) {
        super(plugin, size, deserialize(title));
    }

    public ComponentMenuHolder(final GuiListener guiListener, final P plugin, final int size, final String title) {
        this(guiListener, plugin, size, MINI_MESSAGE.deserialize(title));
    }

    public ComponentMenuHolder(final GuiListener guiListener, final P plugin, final int size, final Component title) {
        super(guiListener, plugin, size, deserialize(title));
    }

    public ComponentMenuHolder(final P plugin, final InventoryType type) {
        super(plugin, type);
    }

    public ComponentMenuHolder(final GuiListener guiListener, final P plugin, final InventoryType type) {
        super(guiListener, plugin, type);
    }

    public ComponentMenuHolder(final P plugin, final int size) {
        super(plugin, size);
    }

    public ComponentMenuHolder(final GuiListener guiListener, final P plugin, final int size) {
        super(guiListener, plugin, size);
    }

    public ComponentMenuHolder(final P plugin, final Inventory inventory) {
        super(plugin, inventory);
    }

    public ComponentMenuHolder(final GuiListener guiListener, final P plugin, final Inventory inventory) {
        super(guiListener, plugin, inventory);
    }

    private static String deserialize(final Component title) {
        return LegacyComponentSerializer.legacySection().serialize(title);
    }
}
