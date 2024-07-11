package de.aethos.lib.option;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@SuppressWarnings("unused")

public sealed interface BoolOption permits None, SomeBool {

    @NotNull
    BoolOption filter(boolean expected);

    boolean orElse(boolean def);

    <U> @NotNull Option<U> map(Supplier<U> callable);

    <U> @NotNull Option<U> flatmap(Supplier<Option<? extends U>> mapper);


}
