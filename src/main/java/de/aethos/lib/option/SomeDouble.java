package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

public record SomeDouble(double value) implements DoubleOption {
    @Override
    public @NotNull DoubleOption filter(@NotNull DoublePredicate predicate) {
        return predicate.test(value) ? Option.some(value) : Option.none();
    }

    @Override
    public double orElse(double def) {
        return value;
    }

    @Override
    public @NotNull <U> Option<U> map(DoubleFunction<? extends U> mapper) {
        return Option.some(mapper.apply(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <U> Option<U> flatmap(DoubleFunction<? extends Option<? extends U>> mapper) {
        return (Option<U>) mapper.apply(value);
    }

    @Override
    public @NotNull DoubleStream doubleStream() {
        return DoubleStream.of(value);
    }
}
