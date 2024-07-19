package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public sealed interface IntOption permits None, SomeInt {

    @NotNull
    IntOption filter(@NotNull IntPredicate predicate);

    int orElse(int def);

    <U> @NotNull Option<U> map(IntFunction<? extends U> mapper);

    <U> @NotNull Option<U> flatmap(IntFunction<? extends Option<? extends U>> mapper);

    @NotNull
    IntStream intStream();

    @NotNull
    LongOption asLong();

    @NotNull
    DoubleOption asDouble();

}
