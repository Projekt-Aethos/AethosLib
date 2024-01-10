package de.aethos.lib.level.interfaces;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LevelClassHolder<ID> {
    /**
     * Gets the identifier for this holder.
     *
     * @return unique identifier
     */
    @NotNull ID identifier();

    /**
     * Gets the LevelClass of this holder.
     *
     * @param key    identifier of the LevelClass
     * @param create if a new LevelClass should be created if none is present
     * @return the attached LevelClass
     */
    @Contract("_, true -> !null")
    @Nullable LevelClass get(@NotNull NamespacedKey key, boolean create);

}
