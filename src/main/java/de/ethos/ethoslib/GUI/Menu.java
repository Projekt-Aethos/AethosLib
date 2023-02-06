package de.ethos.ethoslib.GUI;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;

public abstract class Menu implements Listener {
    final Inventory inventory;
    final Player player;
    final Component title;
    //UUID des Menus dient nur der unterscheidung zu anderen Menüs
    protected final UUID uuid;

    static final ItemStack background = getNamedItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1), "§0.");
    static final ItemStack Placeholder_blue = getNamedItem(new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1), "§0.");

    public Menu() {
        this.inventory = null;
        this.player = null;
        this.title = null;
        this.uuid = null;
    }

    Menu(@NotNull Player player, @NotNull Component title, int rows) {
        this.player = player;
        this.inventory = Bukkit.createInventory(player, rows * 9, title);
        this.title = title;
        this.uuid = UUID.randomUUID();
        inventory.setItem(8, exit());
    }

    public UUID getUUID() {
        return uuid;
    }

    void open() {
        new GUIView(this);
    }

    void initRahmen() {
        int[] indexes_gray = {0, 1, 2, 3, 5, 6, 7, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
        setItem(indexes_gray, background);
    }

    protected ItemStack rahmen() {
        return background;
    }

    protected ItemStack exit() {
        return getGUIItem(new ItemStack(Material.BARRIER, 1), "§4Schließen", "exit");
    }

    void initBeschreibung(String name, String... beschreibungstext) {
        ItemStack beschreibung = getGUIItem(new ItemStack(Material.PAPER, 1), name, "GUI");
        ItemMeta beschreibungMeta = beschreibung.getItemMeta();
        ArrayList<Component> lore = new ArrayList<>();
        for (String text : beschreibungstext)
            lore.add(Component.text(text));
        beschreibungMeta.lore(lore);
        beschreibung.setItemMeta(beschreibungMeta);
        inventory.setItem(4, beschreibung);
    }

    void initUntermenü(Material material, String name, int slot) {
        ItemStack item = getGUIItem(new ItemStack(material, 1), name, name);
        inventory.setItem(slot, item);
    }

    protected static ItemStack getGUIItem(ItemStack itemStack, String name, String protected_gui_value) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        meta.getPersistentDataContainer().set(GUIListener.typ.GUI.namespacedKey, PersistentDataType.STRING, protected_gui_value);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    protected static ItemStack getNamedItem(ItemStack itemStack, String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    void setItem(int[] indexes, ItemStack item) {
        for (int i : indexes)
            inventory.setItem(i, item);
    }

    @EventHandler
    public void openOnItem(InventoryClickEvent e) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!(e.getView() instanceof GUIView)) {
            return;
        }
        if (e.getCurrentItem() == null) {
            return;
        }

        if (getClass().getSimpleName().equals(GUIListener.getProtectedGUIItemValue(e.getCurrentItem().getItemMeta()))) {
            e.setCancelled(true);
            new GUIView(this.getClass().getDeclaredConstructor(Player.class).newInstance((Player) e.getWhoClicked()));
        }
    }

    @EventHandler
    public abstract void onKlick(@NotNull InventoryClickEvent e);

    /**
     * Überprüft ob dieses das geklickte GUI ist
     * Standardüberprüfung für {@link #onKlick(InventoryClickEvent)}
     * Cancelt event, wenn es der Fall ist
     */
    boolean isInvalidClick(@NotNull InventoryClickEvent e) {
        if (!(e.getView() instanceof GUIView gui)) {
            return true;
        }
        e.setCancelled(true);

        if (!gui.getMenu().getClass().isInstance(this)) {
            return true;
        }

        return e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null;
    }

    public Inventory getInventory() {
        return inventory;
    }

}
