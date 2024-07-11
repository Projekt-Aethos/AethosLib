package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record SomeBool(boolean value) implements BoolOption {

    @Override
    public @NotNull BoolOption filter(boolean expected) {
        return Option.some(value == expected);
    }

    @Override
    public boolean orElse(boolean def) {
        return value;
    }

    @Override
    public @NotNull <U> Option<U> map(Supplier<U> callable) {
        return Option.some(callable.get());
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <U> Option<U> flatmap(Supplier<Option<? extends U>> mapper) {
        return (Option<U>) mapper.get();
    }
}
