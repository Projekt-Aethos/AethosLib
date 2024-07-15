package de.aethos.lib.result;

import de.aethos.lib.option.Option;

import java.util.function.Function;
import java.util.stream.Stream;

public record Error<O, E>(E error) implements Result<O, E> {
    @Override
    public <C> C match(Function<? super O, ? extends C> okay, Function<? super E, ? extends C> error) {
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

    @Override
    public <X extends Throwable> O okayOrThrow(Function<E, ? extends X> exception) throws X {
        throw exception.apply(error);
    }

    @Override
    public <X extends Throwable> E errorOrThrow(Function<O, ? extends X> exception) {
        return error;
    }


}
