package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public record None<T>() implements Option<T>, IntOption, LongOption, BoolOption, DoubleOption {
    static final None<?> GENERIC_NONE = new None<>();

    @Override
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        throw exceptionSupplier.get();
    }

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
    public Option<T> filter(Predicate<? super T> predicate) {
        return this;
    }

    @Override
    public T orElse(T def) {
        return def;
    }


    @Override
    public void ifPresent(Consumer<? super T> consumer) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public Option<T> or(Supplier<? extends Option<? extends T>> supplier) {
        return (Option<T>) supplier.get();
    }

    @Override
    public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
        return Option.none();
    }


    @Override
    public <U> Option<U> flatmap(Function<? super T, ? extends Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public IntOption filter(IntPredicate predicate) {
        return Option.none();
    }

    @Override
    public int orElse(int def) {
        return def;
    }

    @Override
    public <U> Option<U> map(IntFunction<? extends U> mapper) {
        return Option.none();
    }


    @Override
    public <U> Option<U> flatmap(IntFunction<? extends Option<? extends U>> mapper) {
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
    public DoubleOption filter(DoublePredicate predicate) {
        return Option.none();
    }

    @Override
    public double orElse(double def) {
        return def;
    }

    @Override
    public <U> Option<U> map(DoubleFunction<? extends U> mapper) {
        return Option.none();
    }

    @Override
    public <U> Option<U> flatmap(DoubleFunction<? extends Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public DoubleStream doubleStream() {
        return DoubleStream.empty();
    }

    @Override
    public BoolOption filter(boolean expected) {
        return Option.none();
    }

    @Override
    public boolean orElse(boolean def) {
        return def;
    }


    @Override
    public <U> Option<U> map(Supplier<U> callable) {
        return Option.none();
    }

    @Override
    public @NotNull <U> Option<U> flatmap(Supplier<Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public boolean orTrue() {
        return true;
    }

    @Override
    public boolean orFalse() {
        return false;
    }


    @Override
    public LongOption filter(LongPredicate predicate) {
        return Option.none();
    }

    @Override
    public long orElse(long def) {
        return def;
    }

    @Override
    public <U> Option<U> map(LongFunction<? extends U> mapper) {
        return Option.none();
    }

    @Override
    public <U> Option<U> flatmap(LongFunction<? extends Option<? extends U>> mapper) {
        return Option.none();
    }

    @Override
    public LongStream LongStream() {
        return LongStream.empty();
    }
}
