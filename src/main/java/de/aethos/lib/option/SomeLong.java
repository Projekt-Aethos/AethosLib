package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;

public record SomeLong(long value) implements LongOption {

    @Override
    public @NotNull LongOption filter(@NotNull LongPredicate predicate) {
        return predicate.test(value) ? Option.some(value) : Option.none();
    }

    @Override
    public long orElse(long def) {
        return value;
    }

    @Override
    public @NotNull <U> Option<U> map(LongFunction<? extends U> mapper) {
        return Option.some(mapper.apply(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <U> Option<U> flatmap(LongFunction<? extends Option<? extends U>> mapper) {
        return (Option<U>) mapper.apply(value);
    }

    @Override
    public @NotNull LongStream LongStream() {
        return LongStream.of(value);
    }
}
