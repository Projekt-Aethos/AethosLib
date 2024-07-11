package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;

@SuppressWarnings("unused")
public sealed interface LongOption permits None, SomeLong {
    @NotNull
    LongOption filter(@NotNull LongPredicate predicate);

    long orElse(long def);

    <U> @NotNull Option<U> map(LongFunction<? extends U> mapper);

    <U> @NotNull Option<U> flatmap(LongFunction<? extends Option<? extends U>> mapper);

    @NotNull
    LongStream LongStream();

}
