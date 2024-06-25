package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record None<T>() implements Option<T> {
    static final None<?> GENERIC_NONE = new None<>();

    @Override
    public @NotNull Option<T> filter(@NotNull Predicate<? super T> predicate) {
        return this;
    }

    @Override
    public @NotNull T orElse(@NotNull T value) {
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Option<T> or(@NotNull Supplier<? extends Option<? extends T>> supplier) {
        return (Option<T>) supplier.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <U> Option<U> map(@NotNull Function<? super T, ? extends U> mapper) {
        return (Option<U>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <U> Option<U> flatmap(@NotNull Function<? super T, ? extends Option<? extends U>> mapper) {
        return (Option<U>) this;
    }

    @Override
    public @NotNull Stream<T> stream() {
        return Stream.empty();
    }
}
