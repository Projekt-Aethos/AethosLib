package de.aethos.lib.option;

import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;

public record SomeLong(long value) implements LongOption {
    @Override
    public LongOption filter(final LongPredicate predicate) {
        return predicate.test(value) ? Option.some(value) : Option.none();
    }

    @Override
    public long orElse(final long def) {
        return value;
    }

    @Override
    public <U> Option<U> map(final LongFunction<? extends U> mapper) {
        return Option.some(mapper.apply(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Option<U> flatmap(final LongFunction<? extends Option<? extends U>> mapper) {
        return (Option<U>) mapper.apply(value);
    }

    @Override
    public LongStream LongStream() {
        return LongStream.of(value);
    }
}
