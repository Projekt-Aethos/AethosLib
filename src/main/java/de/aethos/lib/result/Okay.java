package de.aethos.lib.result;

import de.aethos.lib.option.Option;

import java.util.function.Function;
import java.util.stream.Stream;

public record Okay<O, E>(O value) implements Result<O, E> {
    @Override
    public <C> C match(final Function<? super O, ? extends C> okay, final Function<? super E, ? extends C> error) {
        return okay.apply(this.value);
    }

    @Override
    public Stream<E> streamError() {
        return Stream.empty();
    }

    @Override
    public Stream<O> streamOkay() {
        return Stream.of(value);
    }

    @Override
    public Option<E> optionError() {
        return Option.none();
    }

    @Override
    public Option<O> optionOkay() {
        return Option.some(value);
    }
}
