package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
    public @NotNull T orElse(@NotNull T value) {
        return value;
    }

    @Override
    public @NotNull Option<T> or(@NotNull Supplier<? extends Option<? extends T>> supplier) {
        return this;
    }

    @Override
    public @NotNull <U> Option<U> map(@NotNull Function<? super T, ? extends U> mapper) {
        return new Some<>(mapper.apply(value));
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
}
