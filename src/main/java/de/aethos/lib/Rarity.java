package de.aethos.lib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * The rarity.
 */
public enum Rarity {
    /**
     * The default rarity.
     */
    COMMON(NamedTextColor.WHITE),
    UNCOMMON(NamedTextColor.AQUA),
    RARE(NamedTextColor.GREEN),
    VERY_RARE(NamedTextColor.DARK_GREEN),
    EPIC(NamedTextColor.LIGHT_PURPLE),
    /**
     * The highest rarity someone can get.
     */
    LEGENDARY(NamedTextColor.GOLD),
    /**
     * This should only be obtainable as an exception.
     * <p>Like in a server wide event.
     */
    MYTHIC(NamedTextColor.DARK_RED),
    /**
     * Don't use that, that's my.
     */
    WORLD(TextColor.color(77, 0, 25));

    /**
     * The color of this rarity.
     */
    public final TextColor color;

    Rarity(final TextColor color) {
        this.color = color;
    }

    /**
     * Colors in a gradient of all lesser rarity to this rarity.
     * <p>Note the text needs a greater length for higher rarities to show appropriate.
     *
     * @param toColor to apply the gradient on
     * @return the component with the gradient
     */
    public Component colorGradient(final Component toColor) {
        final MiniMessage mm = MiniMessage.miniMessage();
        final StringBuilder gradientBuilder = new StringBuilder(COMMON.color.asHexString());
        final Rarity[] rarities = Rarity.values();
        for (int i = 1; i < this.ordinal(); i++) {
            gradientBuilder.append(':').append(rarities[i].color.asHexString());
        }
        final String toColorSerialized = mm.serialize(toColor);
        return mm.deserialize("<gradient:" + gradientBuilder + ">" + toColorSerialized + "</gradient>");
    }
}
