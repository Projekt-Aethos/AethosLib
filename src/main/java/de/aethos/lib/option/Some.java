package de.aethos.lib.option;

import java.util.Objects;
import java.util.function.*;

public record Some<T>(T value) implements Option<T> {

    public Some {
        Objects.requireNonNull(value);
    }

    @Override
    public Option<T> filter(Predicate<? super T> predicate) {
        if (predicate.test(value)) {
            return this;
        } else {
            return Option.none();
        }
    }

    @Override
    public T orElse(T def) {
        return this.value;
    }

    @Override
    public Option<T> or(Supplier<? extends Option<? extends T>> supplier) {
        return this;
    }

    @Override
    public <U> Option<U> map(Function<? super T, ? extends U> mapper) {
        return Option.of(mapper.apply(value));
    }

    @Override
    public void ifPresent(Consumer<? super T> consumer) {
        consumer.accept(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Option<U> flatmap(Function<? super T, ? extends Option<? extends U>> mapper) {
        return (Option<U>) Objects.requireNonNullElse(mapper.apply(value), Option.none());
    }

    @Override
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) {
        return value;
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
    

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
