package de.aethos.lib.option;

import java.util.function.Supplier;

public record SomeBool(boolean value) implements BoolOption {
    @Override
    public BoolOption filter(final boolean expected) {
        return Option.some(value == expected);
    }

    @Override
    public boolean orElse(final boolean def) {
        return value;
    }

    @Override
    public <U> Option<U> map(final Supplier<U> callable) {
        return Option.some(callable.get());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Option<U> flatmap(final Supplier<Option<? extends U>> mapper) {
        return (Option<U>) mapper.get();
    }
}
