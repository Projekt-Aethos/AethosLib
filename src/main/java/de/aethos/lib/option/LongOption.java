package de.aethos.lib.option;

import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;

@SuppressWarnings("unused")
public sealed interface LongOption permits None, SomeLong {
    LongOption filter(LongPredicate predicate);

    long orElse(long def);

    <U> Option<U> map(LongFunction<? extends U> mapper);

    <U> Option<U> flatmap(LongFunction<? extends Option<? extends U>> mapper);

    LongStream LongStream();
}
