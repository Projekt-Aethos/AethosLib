package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

@SuppressWarnings("unused")
public sealed interface DoubleOption permits None, SomeDouble {

    @NotNull
    DoubleOption filter(@NotNull DoublePredicate predicate);

    double orElse(double def);

    <U> @NotNull Option<U> map(DoubleFunction<? extends U> mapper);

    <U> @NotNull Option<U> flatmap(DoubleFunction<? extends Option<? extends U>> mapper);

    @NotNull
    DoubleStream doubleStream();


}
