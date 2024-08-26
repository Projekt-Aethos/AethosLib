package de.aethos.lib.result;

import de.aethos.lib.option.Option;

import java.util.function.Function;
import java.util.stream.Stream;

public record Error<O, E>(E error) implements Result<O, E> {
    @Override
    public <C> C match(final Function<? super O, ? extends C> okay, final Function<? super E, ? extends C> error) {
        return error.apply(this.error);
    }

    @Override
    public Stream<E> streamError() {
        return Stream.of(error);
    }

    @Override
    public Stream<O> streamOkay() {
        return Stream.empty();
    }

    @Override
    public Option<E> optionError() {
        return Option.some(error);
    }

    @Override
    public Option<O> optionOkay() {
        return Option.none();
    }
}
