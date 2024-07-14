package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.*;
import java.util.stream.Stream;

public record Some<T>(@NotNull T value) implements Option<T> {

    @Override
    public @NotNull Option<T> filter(@NotNull Predicate<? super T> predicate) {
        if (predicate.test(value)) {
            return this;
        } else {
            return Option.none();
        }
    }

    @Override
    public @NotNull T orElse(@NotNull T def) {
        return this.value;
    }

    @Override
    public @NotNull Option<T> or(@NotNull Supplier<? extends Option<? extends T>> supplier) {
        return this;
    }

    @Override
    public @NotNull <U> Option<U> map(@NotNull Function<? super T, ? extends U> mapper) {
        return Option.of(mapper.apply(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <U> Option<U> flatmap(@NotNull Function<? super T, ? extends Option<? extends U>> mapper) {
        return (Option<U>) Objects.requireNonNullElse(mapper.apply(value), Option.none());
    }

    @Override
    public @NotNull Stream<T> stream() {
        return Stream.of(value);
    }

    @Override
    public IntOption toInt(ToIntFunction<T> function) {
        return Option.some(function.applyAsInt(value));
    }

    @Override
    public LongOption toLong(ToLongFunction<T> function) {
        return Option.some(function.applyAsLong(value));
    }

    @Override
    public BoolOption toBool(Predicate<T> predicate) {
        return Option.some(predicate.test(value));
    }

    @Override
    public DoubleOption toDouble(ToDoubleFunction<T> function) {
        return Option.some(function.applyAsDouble(value));
    }
}
