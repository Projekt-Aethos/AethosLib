package de.aethos.lib.ui.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility to create ItemStacks.
 * <p>
 * Every builder method in this class returns a new ItemBuilder instance
 * so that instances can be reused.
 *
 * @see xyz.janboerman.guilib.api.ItemBuilder for the source
 */
@SuppressWarnings("unused")
public class ItemBuilder {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final ItemStack itemStack;

    /**
     * Creates a new ItemBuilder with the given Material.
     *
     * @param material the Material
     */
    public ItemBuilder(final Material material) {
        this.itemStack = new ItemStack(Objects.requireNonNull(material, "Material cannot be null"));
    }

    /**
     * Creates a new ItemBuilder with the given ItemStack.
     * The ItemBuilder clones the ItemStack so that it won't change its state.
     *
     * @param itemStack the ItemStack
     */
    public ItemBuilder(final ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack, "ItemStack cannot be null").clone();
    }

    /**
     * Specify the number of items being built.
     *
     * @param amount the number of items
     * @return a new ItemBuilder
     */
    public ItemBuilder amount(final int amount) {
        return change(i -> i.setAmount(amount));
    }

    /**
     * Specify an enchantment for the items being built.
     *
     * @param enchantment the enchantment type
     * @param level       the enchantment level
     * @return a new ItemBuilder
     */
    public ItemBuilder enchant(final Enchantment enchantment, final int level) {
        return change(i -> i.addUnsafeEnchantment(enchantment, level));
    }

    /**
     * Specify that an enchanment is removed from the item being built.
     *
     * @param enchantment the enchantment type to remove
     * @return a new ItemBuilder
     */
    public ItemBuilder unEnchant(final Enchantment enchantment) {
        return change(i -> i.removeEnchantment(enchantment));
    }

    /**
     * Specify the damage for the items being built.
     *
     * @param damage the damage (0 = full health)
     * @return a new ItemBuilder
     * @throws ClassCastException when the item is not damageable
     */
    public ItemBuilder damage(final int damage) {
        return changeMeta(meta -> ((Damageable) meta).setDamage(damage));
    }

    /**
     * Specify the type of the items being built.
     *
     * @param type the type
     * @return a new ItemBuilder
     */
    public ItemBuilder type(final Material type) {
        return new ItemBuilder(itemStack.withType(type));
    }

    /**
     * Specify the display name of the items being built.
     *
     * @param displayName the display name in MiniMessage format
     * @return a new ItemBuilder
     */
    public ItemBuilder displayName(final String displayName) {
        return displayName(MINI_MESSAGE.deserialize(displayName));
    }

    /**
     * Specify the display name of the items being built.
     *
     * @param displayName the display name
     * @return a new ItemBuilder
     */
    public ItemBuilder displayName(final Component displayName) {
        return changeMeta(itemMeta -> itemMeta.displayName(displayName));
    }

    /**
     * Specify the item name of the items being built.
     *
     * @param displayName the item name
     * @return a new ItemBuilder
     */
    public ItemBuilder itemName(final String displayName) {
        return itemName(MINI_MESSAGE.deserialize(displayName));
    }

    /**
     * Specify the item name of the items being built.
     *
     * @param displayName the item name
     * @return a new ItemBuilder
     */
    public ItemBuilder itemName(final Component displayName) {
        return changeMeta(itemMeta -> itemMeta.itemName(displayName));
    }

    /**
     * Specify the lore of the items being built.
     *
     * @param lore the lore in MiniMessage format
     * @return a new ItemBuilder
     */
    public ItemBuilder loreMM(final List<String> lore) {
        return lore(lore.stream().map(MINI_MESSAGE::deserialize).toList());
    }

    /**
     * Specify the lore of the items being built.
     *
     * @param lore the lore
     * @return a new ItemBuilder
     */
    public ItemBuilder lore(final List<Component> lore) {
        return changeMeta(meta -> meta.lore(lore));
    }

    /**
     * Specify the lore of the items being built.
     *
     * @param lore the lore in MiniMessage format
     * @return a new ItemBuilder
     */
    public ItemBuilder lore(final String... lore) {
        return loreMM(List.of(lore));
    }

    /**
     * Specify the lore of the items being built.
     *
     * @param lore the lore
     * @return a new ItemBuilder
     */
    public ItemBuilder lore(final Component... lore) {
        return lore(List.of(lore));
    }

    /**
     * Add a line of lore to the items being built.
     *
     * @param line the lore in MiniMessage format
     * @return a new ItemBuilder
     */
    public ItemBuilder addLore(final String line) {
        return addLore(MINI_MESSAGE.deserialize(line));
    }

    /**
     * Add a line of lore to the items being built.
     *
     * @param line the lore
     * @return a new ItemBuilder
     */
    public ItemBuilder addLore(final Component line) {
        final List<Component> lore = itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore() ?
                new ArrayList<>(itemStack.getItemMeta().lore()) : new ArrayList<>();
        lore.add(line);
        return lore(lore);
    }

    /**
     * Specify that the items being built are unbreakable.
     *
     * @return a new ItemBuilder
     */
    public ItemBuilder unbreakable() {
        return unbreakable(true);
    }

    /**
     * Specify that the items builing built are (un)breakable.
     *
     * @param unbreakable unbreakable when true, breakable when false
     * @return a new ItemBuilder
     */
    public ItemBuilder unbreakable(final boolean unbreakable) {
        return changeMeta(meta -> meta.setUnbreakable(unbreakable));
    }

    /**
     * Specify the item flags for the items being built.
     *
     * @param flags the flags
     * @return a new ItemBuilder
     */
    public ItemBuilder flags(final ItemFlag... flags) {
        return changeMeta(meta -> meta.addItemFlags(flags));
    }

    /**
     * Specify the attribute modifiers for the items being built.
     *
     * @param attributeModifiers the attribute modifiers
     * @return a new ItemBuilder
     */
    public ItemBuilder attributes(final Multimap<Attribute, AttributeModifier> attributeModifiers) {
        return changeMeta(meta -> meta.setAttributeModifiers(attributeModifiers));
    }

    /**
     * Specify the attribute modifiers for the items being built.
     *
     * @param attributeModifiers the attribute modifiers
     * @return a new ItemBuilder
     */
    public ItemBuilder attributes(final Map.Entry<Attribute, AttributeModifier>... attributeModifiers) {
        return attributes(ImmutableMultimap.copyOf(List.of(attributeModifiers)));
    }

    /**
     * Specify an attribute for the items being built.
     *
     * @param attribute         the attribute type
     * @param attributeModifier the attribute modifier
     * @return a new ItemBuilder
     */
    public ItemBuilder addAttribute(final Attribute attribute, final AttributeModifier attributeModifier) {
        return changeMeta(meta -> meta.addAttributeModifier(attribute, attributeModifier));
    }

    /**
     * Specify attributes to be added to the items being built.
     *
     * @param attributeModifiers the attribute modifiers
     * @return a new ItemBuilder
     */
    public ItemBuilder addAttributes(final Multimap<Attribute, AttributeModifier> attributeModifiers) {
        return attributeModifiers.entries().stream()
                .reduce(this, (itemBuilder, entry) -> itemBuilder.addAttribute(entry.getKey(), entry.getValue()),
                        (first, second) -> second);
    }

    /**
     * Specify attributes to be added to the items being built.
     *
     * @param attributeModifiers the attribute modifiers
     * @return a new ItemBuilder
     */
    public ItemBuilder addAttributes(final Map.Entry<Attribute, AttributeModifier>... attributeModifiers) {
        ItemBuilder result = this;
        for (final var entry : attributeModifiers) {
            result = result.addAttribute(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Set custom model data for the items being built.
     *
     * @param customModelData an integer that may be associated client side with a custom item model
     * @return a new ItemBuilder
     */
    public ItemBuilder customModelData(final Integer customModelData) {
        return changeMeta(meta -> meta.setCustomModelData(customModelData));
    }

    /**
     * Change the persistent data of the ItemMeta of the ItemStack being built.
     *
     * @param consumer the PersistentDataContainer consumer
     * @return a new ItemBuilder
     * @see #persistentData(NamespacedKey, PersistentDataType, Object)
     */
    public ItemBuilder changePersistentData(final Consumer<? super PersistentDataContainer> consumer) {
        return changeMeta(meta -> consumer.accept(meta.getPersistentDataContainer()));
    }

    /**
     * Set custom data on the item that will persist when the item is being transferred to different inventories,
     * falls on the ground and will persist after server restarts.
     *
     * @param key   the key of the data
     * @param type  the type of the data
     * @param value the value of the data
     * @param <T>   the data's primitive type
     * @param <Z>   the data's complex type
     * @return a new ItemBuilder
     * @see #persistentData(NamespacedKey, byte)
     * @see #persistentData(NamespacedKey, byte[])
     * @see #persistentData(NamespacedKey, double)
     * @see #persistentData(NamespacedKey, float)
     * @see #persistentData(NamespacedKey, int)
     * @see #persistentData(NamespacedKey, int[])
     * @see #persistentData(NamespacedKey, long)
     * @see #persistentData(NamespacedKey, long[])
     * @see #persistentData(NamespacedKey, short)
     * @see #persistentData(NamespacedKey, String)
     * @see #persistentData(NamespacedKey, PersistentDataContainer)
     * @see PersistentDataType#LIST
     */
    public <T, Z> ItemBuilder persistentData(final NamespacedKey key, final PersistentDataType<T, Z> type, final Z value) {
        return changePersistentData(data -> data.set(key, type, value));
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the byte type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final byte value) {
        return persistentData(key, PersistentDataType.BYTE, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the byte[] type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final byte[] value) {
        return persistentData(key, PersistentDataType.BYTE_ARRAY, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the double type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final double value) {
        return persistentData(key, PersistentDataType.DOUBLE, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the float type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final float value) {
        return persistentData(key, PersistentDataType.FLOAT, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the int type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final int value) {
        return persistentData(key, PersistentDataType.INTEGER, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the int[] type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final int[] value) {
        return persistentData(key, PersistentDataType.INTEGER_ARRAY, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the long type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final long value) {
        return persistentData(key, PersistentDataType.LONG, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the long[] type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final long[] value) {
        return persistentData(key, PersistentDataType.LONG_ARRAY, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the short type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final short value) {
        return persistentData(key, PersistentDataType.SHORT, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the String type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final String value) {
        return persistentData(key, PersistentDataType.STRING, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the PersistentDataContainer type.
     *
     * @param key   the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(final NamespacedKey key, final PersistentDataContainer value) {
        return persistentData(key, PersistentDataType.TAG_CONTAINER, value);
    }

    /**
     * Customise meta for the items being built. Example:
     *
     * <pre> {@code
     * ItemStack bookStack = new ItemBuilder(Material.WRITTEN_BOOK)
     *         .changeMeta((BookMeta bookMeta) -> bookMeta.setAuthor("Jannyboy11"))
     *         .build();
     * }</pre>
     * <p>
     * This method is especially helpful when dealing with subclasses of {@link ItemMeta}.
     *
     * @param consumer the ItemMeta consumer
     * @param <IM>     the type of meta
     * @return a new ItemBuilder
     */
    @SuppressWarnings("unchecked")
    public <IM extends ItemMeta> ItemBuilder changeMeta(final Consumer<IM> consumer) {
        return changeItemMeta(m -> consumer.accept((IM) m));
    }

    /**
     * Customise the meta for items being built.
     *
     * @param consumer the ItemMeta consumer
     * @return a new ItemBuilder
     */
    public ItemBuilder changeItemMeta(final Consumer<? super ItemMeta> consumer) {
        return change(i -> {
            final ItemMeta meta = i.getItemMeta();
            consumer.accept(meta);
            i.setItemMeta(meta);
        });
    }

    /**
     * Customise the items being built.
     *
     * @param consumer the item consumer
     * @return a new ItemBuilder
     */
    public ItemBuilder change(final Consumer<? super ItemStack> consumer) {
        final ItemBuilder builder = new ItemBuilder(itemStack);
        consumer.accept(builder.itemStack);
        return builder;
    }

    /**
     * Apply a function of type ItemStack -&gt; ItemStack to the items being built.
     *
     * @param function the ItemStack mapper
     * @return a new ItemBuilder
     */
    public ItemBuilder map(final Function<? super ItemStack, ? extends ItemStack> function) {
        return new ItemBuilder(function.apply(build()));
    }

    /**
     * Builds the ItemStack.
     *
     * @return the result of this builder
     */
    public ItemStack build() {
        return itemStack.clone();
    }
}
