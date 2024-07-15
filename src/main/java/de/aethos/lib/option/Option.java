package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.*;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public sealed interface Option<T> permits None, Some {
    @SuppressWarnings("unchecked")
    static <T> @NotNull None<T> none() {
        return (None<T>) None.GENERIC_NONE;
    }

    static <T> @NotNull Option<T> some(@NotNull T value) {
        return new Some<>(value);
    }

    static <T> @NotNull Option<T> of(@Nullable T value) {
        return value == null ? none() : some(value);
    }

    static @NotNull Option<Integer> from(IntOption option) {
        if (option instanceof SomeInt some) {
            return some(Integer.valueOf(some.value()));
        }
        return none();
    }

    static @NotNull Option<Double> from(DoubleOption option) {
        if (option instanceof SomeDouble some) {
            return some(Double.valueOf(some.value()));
        }
        return none();
    }

    static @NotNull Option<Long> from(LongOption option) {
        if (option instanceof SomeLong some) {
            return some(Long.valueOf(some.value()));
        }
        return none();
    }

    static @NotNull Option<Boolean> from(BoolOption option) {
        if (option instanceof SomeBool some) {
            return some(Boolean.valueOf(some.value()));
        }
        return none();
    }

    static @NotNull IntOption some(int value) {
        return new SomeInt(value);
    }

    static @NotNull LongOption some(long value) {
        return new SomeLong(value);
    }

    static @NotNull BoolOption some(boolean value) {
        return new SomeBool(value);
    }

    static @NotNull DoubleOption some(double value) {
        return new SomeDouble(value);
    }

    <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X;

    IntOption toInt(ToIntFunction<T> function);

    LongOption toLong(ToLongFunction<T> function);

    BoolOption toBool(Predicate<T> predicate);

    DoubleOption toDouble(ToDoubleFunction<T> function);

    @NotNull
    Option<T> filter(@NotNull Predicate<? super T> predicate);

    @NotNull
    T orElse(@NotNull T def);

    @NotNull
    Option<T> or(@NotNull Supplier<? extends Option<? extends T>> supplier);

    <U> @NotNull Option<U> map(@NotNull Function<? super T, ? extends U> mapper);

    <U> @NotNull Option<U> flatmap(@NotNull Function<? super T, ? extends Option<? extends U>> mapper);

    @NotNull
    Stream<T> stream();


}
