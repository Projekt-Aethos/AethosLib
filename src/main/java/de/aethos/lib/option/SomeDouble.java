package de.aethos.lib.option;

import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

public record SomeDouble(double value) implements DoubleOption {
    @Override
    public DoubleOption filter(final DoublePredicate predicate) {
        return predicate.test(value) ? Option.some(value) : Option.none();
    }

    @Override
    public double orElse(final double def) {
        return value;
    }

    @Override
    public <U> Option<U> map(final DoubleFunction<? extends U> mapper) {
        return Option.some(mapper.apply(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Option<U> flatmap(final DoubleFunction<? extends Option<? extends U>> mapper) {
        return (Option<U>) mapper.apply(value);
    }

    @Override
    public DoubleStream doubleStream() {
        return DoubleStream.of(value);
    }
}
