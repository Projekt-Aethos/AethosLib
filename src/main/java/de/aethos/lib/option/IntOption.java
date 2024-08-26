package de.aethos.lib.option;

import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public sealed interface IntOption permits None, SomeInt {
    IntOption filter(IntPredicate predicate);

    int orElse(int def);

    <U> Option<U> map(IntFunction<? extends U> mapper);

    <U> Option<U> flatmap(IntFunction<? extends Option<? extends U>> mapper);

    IntStream intStream();

    LongOption asLong();

    DoubleOption asDouble();
}
