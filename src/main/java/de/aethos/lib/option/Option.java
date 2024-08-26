package de.aethos.lib.option;

import org.jetbrains.annotations.Nullable;

import java.util.function.*;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public sealed interface Option<T> permits None, Some {
    @SuppressWarnings("unchecked")
    static <T> None<T> none() {
        return (None<T>) None.GENERIC_NONE;
    }

    static <T> Option<T> some(final T value) {
        return new Some<>(value);
    }

    static <T> Option<T> of(@Nullable final T value) {
        return value == null ? none() : some(value);
    }

    static Option<Integer> from(final IntOption option) {
        if (option instanceof SomeInt some) {
            return some(Integer.valueOf(some.value()));
        }
        return none();
    }

    static Option<Double> from(final DoubleOption option) {
        if (option instanceof SomeDouble some) {
            return some(Double.valueOf(some.value()));
        }
        return none();
    }

    static Option<Long> from(final LongOption option) {
        if (option instanceof SomeLong some) {
            return some(Long.valueOf(some.value()));
        }
        return none();
    }

    static Option<Boolean> from(final BoolOption option) {
        if (option instanceof SomeBool some) {
            return some(Boolean.valueOf(some.value()));
        }
        return none();
    }

    static IntOption some(final int value) {
        return new SomeInt(value);
    }

    static LongOption some(final long value) {
        return new SomeLong(value);
    }

    static BoolOption some(final boolean value) {
        return new SomeBool(value);
    }

    static DoubleOption some(final double value) {
        return new SomeDouble(value);
    }

    IntOption toInt(ToIntFunction<T> function);

    LongOption toLong(ToLongFunction<T> function);

    BoolOption toBool(Predicate<T> predicate);

    DoubleOption toDouble(ToDoubleFunction<T> function);

    Option<T> filter(Predicate<? super T> predicate);

    T orElse(T def);

    Option<T> or(Supplier<? extends Option<? extends T>> supplier);

    <U> Option<U> map(Function<? super T, ? extends U> mapper);

    <U> Option<U> flatmap(Function<? super T, ? extends Option<? extends U>> mapper);

    Stream<T> stream();
}
