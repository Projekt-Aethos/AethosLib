package de.aethos.lib.result;

import de.aethos.lib.option.Option;

import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public sealed interface Result<O, E> permits Error, Okay {
    static <O, E> Error<O, E> err(final E error) {
        return new Error<>(error);
    }

    static <O, E> Okay<O, E> ok(final O okay) {
        return new Okay<>(okay);
    }

    <C> C match(Function<? super O, ? extends C> okay, Function<? super E, ? extends C> error);

    Stream<E> streamError();

    Stream<O> streamOkay();

    Option<E> optionError();

    Option<O> optionOkay();
}
