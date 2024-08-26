package de.aethos.lib.option;

import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

@SuppressWarnings("unused")
public sealed interface DoubleOption permits None, SomeDouble {
    DoubleOption filter(DoublePredicate predicate);

    double orElse(double def);

    <U> Option<U> map(DoubleFunction<? extends U> mapper);

    <U> Option<U> flatmap(DoubleFunction<? extends Option<? extends U>> mapper);

    DoubleStream doubleStream();
}
