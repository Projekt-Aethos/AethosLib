package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public record None<T>() implements Option<T>, IntOption, LongOption, BoolOption, DoubleOption {
    static final None<?> GENERIC_NONE = new None<>();

    @Override
    public IntOption toInt(ToIntFunction<T> function) {
        return Option.none();
    }

    @Override
    public LongOption toLong(ToLongFunction<T> function) {
        return Option.none();
    }

    @Override
    public BoolOption toBool(Predicate<T> predicate) {
        return Option.none();
    }

    @Override
    public DoubleOption toDouble(ToDoubleFunction<T> function) {
        return Option.none();
    }

    @Override
    public @NotNull Option<T> filter(@NotNull Predicate<? super T> predicate) {
        return this;
    }

    @Override
    public @NotNull T orElse(@NotNull T def) {
        return def;
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

    @Override
    public @NotNull IntOption filter(@NotNull IntPredicate predicate) {
        return Option.none();
    }

    @Override
    public int orElse(int def) {
        return def;
    }

    @Override
    public @NotNull <U> Option<U> map(IntFunction<? extends U> mapper) {
        return Option.none();
    }


    @Override
    public @NotNull <U> Option<U> flatmap(IntFunction<? extends Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public @NotNull IntStream intStream() {
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
    public @NotNull DoubleOption filter(@NotNull DoublePredicate predicate) {
        return Option.none();
    }

    @Override
    public double orElse(double def) {
        return def;
    }

    @Override
    public @NotNull <U> Option<U> map(DoubleFunction<? extends U> mapper) {
        return Option.none();
    }

    @Override
    public @NotNull <U> Option<U> flatmap(DoubleFunction<? extends Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public @NotNull DoubleStream doubleStream() {
        return DoubleStream.empty();
    }

    @Override
    public @NotNull BoolOption filter(boolean expected) {
        return Option.none();
    }

    @Override
    public boolean orElse(boolean def) {
        return def;
    }

    @Override
    public @NotNull <U> Option<U> map(Supplier<U> callable) {
        return Option.none();
    }

    @Override
    public @NotNull <U> Option<U> flatmap(Supplier<Option<? extends U>> mapper) {
        return Option.none();
    }


    @Override
    public @NotNull LongOption filter(@NotNull LongPredicate predicate) {
        return Option.none();
    }

    @Override
    public long orElse(long def) {
        return def;
    }

    @Override
    public @NotNull <U> Option<U> map(LongFunction<? extends U> mapper) {
        return Option.none();
    }

    @Override
    public @NotNull <U> Option<U> flatmap(LongFunction<? extends Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public @NotNull LongStream LongStream() {
        return LongStream.empty();
    }
}
