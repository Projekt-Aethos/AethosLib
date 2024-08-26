package de.aethos.lib.option;

import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public record None<T>() implements Option<T>, IntOption, LongOption, BoolOption, DoubleOption {
    static final None<?> GENERIC_NONE = new None<>();

    @Override
    public IntOption toInt(final ToIntFunction<T> function) {
        return Option.none();
    }

    @Override
    public LongOption toLong(final ToLongFunction<T> function) {
        return Option.none();
    }

    @Override
    public BoolOption toBool(final Predicate<T> predicate) {
        return Option.none();
    }

    @Override
    public DoubleOption toDouble(final ToDoubleFunction<T> function) {
        return Option.none();
    }

    @Override
    public Option<T> filter(final Predicate<? super T> predicate) {
        return this;
    }

    @Override
    public T orElse(final T def) {
        return def;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Option<T> or(final Supplier<? extends Option<? extends T>> supplier) {
        return (Option<T>) supplier.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Option<U> map(final Function<? super T, ? extends U> mapper) {
        return (Option<U>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Option<U> flatmap(final Function<? super T, ? extends Option<? extends U>> mapper) {
        return (Option<U>) this;
    }

    @Override
    public Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public IntOption filter(final IntPredicate predicate) {
        return Option.none();
    }

    @Override
    public int orElse(final int def) {
        return def;
    }

    @Override
    public <U> Option<U> map(final IntFunction<? extends U> mapper) {
        return Option.none();
    }

    @Override
    public <U> Option<U> flatmap(final IntFunction<? extends Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public IntStream intStream() {
        return IntStream.empty();
    }

    @Override
    public LongOption asLong() {
        return Option.none();
    }

    @Override
    public DoubleOption asDouble() {
        return Option.none();
    }

    @Override
    public DoubleOption filter(final DoublePredicate predicate) {
        return Option.none();
    }

    @Override
    public double orElse(final double def) {
        return def;
    }

    @Override
    public <U> Option<U> map(final DoubleFunction<? extends U> mapper) {
        return Option.none();
    }

    @Override
    public <U> Option<U> flatmap(final DoubleFunction<? extends Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public DoubleStream doubleStream() {
        return DoubleStream.empty();
    }

    @Override
    public BoolOption filter(final boolean expected) {
        return Option.none();
    }

    @Override
    public boolean orElse(final boolean def) {
        return def;
    }

    @Override
    public <U> Option<U> map(final Supplier<U> callable) {
        return Option.none();
    }

    @Override
    public <U> Option<U> flatmap(final Supplier<Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public LongOption filter(final LongPredicate predicate) {
        return Option.none();
    }

    @Override
    public long orElse(final long def) {
        return def;
    }

    @Override
    public <U> Option<U> map(final LongFunction<? extends U> mapper) {
        return Option.none();
    }

    @Override
    public <U> Option<U> flatmap(final LongFunction<? extends Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public LongStream LongStream() {
        return LongStream.empty();
    }
}
