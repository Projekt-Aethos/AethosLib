package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public sealed interface Option<T> permits Some, None {


    @SuppressWarnings("unchecked")
    static <T> @NotNull Option<T> none() {
        return (Option<T>) None.GENERIC_NONE;
    }

    static <T> @NotNull Option<T> some(@NotNull T value) {
        return new Some<>(value);
    }

    static <T> @NotNull Option<T> of(@Nullable T value) {
        return value == null ? none() : some(value);
    }

    @NotNull
    Option<T> filter(@NotNull Predicate<? super T> predicate);

    @NotNull
    T orElse(@NotNull T value);

    @NotNull
    Option<T> or(@NotNull Supplier<? extends Option<? extends T>> supplier);

    <U> @NotNull Option<U> map(@NotNull Function<? super T, ? extends U> mapper);

    <U> @NotNull Option<U> flatmap(@NotNull Function<? super T, ? extends Option<? extends U>> mapper);

    @NotNull
    Stream<T> stream();

}
