package de.aethos.lib.option;

import java.util.function.Supplier;

@SuppressWarnings("unused")

public sealed interface BoolOption permits None, SomeBool {
    BoolOption filter(boolean expected);

    boolean orElse(boolean def);

    <U> Option<U> map(Supplier<U> callable);

    <U> Option<U> flatmap(Supplier<Option<? extends U>> mapper);
}
