package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public record SomeInt(int value) implements IntOption {

    @Override
    public @NotNull IntOption filter(@NotNull IntPredicate predicate) {
        return predicate.test(value) ? Option.some(value) : Option.none();
    }

    @Override
    public int orElse(int def) {
        return this.value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <U> Option<U> flatmap(IntFunction<? extends Option<? extends U>> mapper) {
        return (Option<U>) Objects.requireNonNullElse(mapper.apply(value), Option.none());
    }

    @Override
    public @NotNull <U> Option<U> map(IntFunction<? extends U> mapper) {
        return Option.some(mapper.apply(value));
    }

    @Override
    public @NotNull IntStream intStream() {
        return IntStream.of(value);
    }

    @Override
    public @NotNull LongOption asLong() {
        return Option.some((long) value);
    }

    @Override
    public @NotNull DoubleOption asDouble() {
        return Option.some((double) value);
    }


}
