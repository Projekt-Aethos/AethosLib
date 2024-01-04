package de.aethos.lib.result;

import de.aethos.lib.option.Option;

import java.util.function.Function;
import java.util.stream.Stream;

public record Okay<O, E>(O okay) implements Result<O, E> {
    @Override
    public <C> C match(Function<? super O, ? extends C> okay, Function<? super E, ? extends C> error) {
        return okay.apply(this.okay);
    }

    @Override
    public Stream<E> streamError() {
        return Stream.empty();
    }

    @Override
    public Stream<O> streamOkay() {
        return Stream.of(okay);
    }

    @Override
    public Option<E> optionError() {
        return Option.none();
    }

    @Override
    public Option<O> optionOkay() {
        return Option.some(okay);
    }
}
