package de.aethos.lib.option;

import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

public record Some<T>(T value) implements Option<T> {
    @Override
    public IntOption toInt(final ToIntFunction<T> function) {
        return Option.some(function.applyAsInt(value));
    }

    @Override
    public LongOption toLong(final ToLongFunction<T> function) {
        return Option.some(function.applyAsLong(value));
    }

    @Override
    public BoolOption toBool(final Predicate<T> predicate) {
        return Option.some(predicate.test(value));
    }

    @Override
    public DoubleOption toDouble(final ToDoubleFunction<T> function) {
        return Option.some(function.applyAsDouble(value));
    }

    @Override
    public Option<T> filter(final Predicate<? super T> predicate) {
        if (predicate.test(value)) {
            return this;
        } else {
            return Option.none();
        }
    }

    @Override
    public T orElse(final T def) {
        return this.value;
    }

    @Override
    public Option<T> or(final Supplier<? extends Option<? extends T>> supplier) {
        return this;
    }

    @Override
    public <U> Option<U> map(final Function<? super T, ? extends U> mapper) {
        return Option.of(mapper.apply(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Option<U> flatmap(final Function<? super T, ? extends Option<? extends U>> mapper) {
        return (Option<U>) Objects.requireNonNullElse(mapper.apply(value), Option.none());
    }

    @Override
    public Stream<T> stream() {
        return Stream.of(value);
    }
}
