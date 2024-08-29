package de.aethos.lib.tuple.pair;

import java.util.Objects;
import java.util.function.Function;

public final class MutablePair<T1, T2> implements Pair<T1, T2> {

    private T1 first;
    private T2 second;

    public MutablePair(T1 first, T2 second) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    @Override
    public <U> MutablePair<U, T2> mapFirst(Function<? super T1, ? extends U> function) {
        return new MutablePair<>(function.apply(first), second);
    }

    @Override
    public <U> MutablePair<T1, U> mapSecond(Function<? super T2, ? extends U> function) {
        return new MutablePair<>(first, function.apply(second));
    }

    @Override
    public <U1, U2> MutablePair<U1, U2> map(Function<? super T1, ? extends U1> function1, Function<? super T2, ? extends U2> function2) {
        return new MutablePair<>(function1.apply(first), function2.apply(second));
    }

    @Override
    public MutablePair<T2, T1> change() {
        return new MutablePair<>(second, first);
    }

    @SuppressWarnings("unused")
    public void first(T1 first) {
        this.first = Objects.requireNonNull(first);
    }

    @SuppressWarnings("unused")
    public void second(T2 second) {
        this.second = Objects.requireNonNull(second);
    }

    @Override
    public T1 first() {
        return first;
    }

    @Override
    public T2 second() {
        return second;
    }


    @Override
    public MutablePair<T1, T2> asMutablePair() {
        return this;
    }

    @Override
    public ImmutablePair<T1, T2> asImmutablePair() {
        return new ImmutablePair<>(first, second);
    }


}
