package de.aethos.lib.callbacks;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;

import java.util.List;
import java.util.function.Function;

@FunctionalInterface
public interface CallBackResolver extends Function<String, ClickCallback<Audience>> {
    @Override
    ClickCallback<Audience> apply(String string);

    default List<String> tabCompletion(String str) {
        return List.of();
    }

}
