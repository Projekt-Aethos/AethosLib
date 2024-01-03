package de.aethos.lib.option;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public sealed interface Option<T> permits Some, None {
    @ApiStatus.Internal
    None<?> GENERIC_NONE = new None<>();

    @SuppressWarnings("unchecked")

    static <T> @NotNull Option<T> none() {
        return (Option<T>) GENERIC_NONE;
    }

    static <T> @NotNull Option<T> some(@NotNull T value) {
        return new Some<>(value);
    }


    @NotNull Option<T> filter(@NotNull Predicate<? super T> predicate);

    @NotNull T orElse(@NotNull T value);

    @NotNull Option<T> or(@NotNull Supplier<? extends Option<? extends T>> supplier);

    <U> @NotNull Option<U> map(@NotNull Function<? super T, ? extends U> mapper);

    <U> @NotNull Option<U> flatmap(@NotNull Function<? super T, ? extends Option<? extends U>> mapper);

    @NotNull Stream<T> stream();

}
