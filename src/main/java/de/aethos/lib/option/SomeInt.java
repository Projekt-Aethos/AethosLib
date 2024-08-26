package de.aethos.lib.option;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public record SomeInt(int value) implements IntOption {

    @Override
    public IntOption filter(final IntPredicate predicate) {
        return predicate.test(value) ? Option.some(value) : Option.none();
    }

    @Override
    public int orElse(final int def) {
        return this.value;
    }

    @Override
    public <U> Option<U> map(final IntFunction<? extends U> mapper) {
        return Option.some(mapper.apply(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Option<U> flatmap(final IntFunction<? extends Option<? extends U>> mapper) {
        return (Option<U>) Objects.requireNonNullElse(mapper.apply(value), Option.none());
    }

    @Override
    public IntStream intStream() {
        return IntStream.of(value);
    }

    @Override
    public LongOption asLong() {
        return Option.some((long) value);
    }

    @Override
    public DoubleOption asDouble() {
        return Option.some((double) value);
    }
}
