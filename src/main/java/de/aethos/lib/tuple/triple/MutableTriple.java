package de.aethos.lib.tuple.triple;

import java.util.Objects;
import java.util.function.Function;

public final class MutableTriple<T1, T2, T3> implements Triple<T1, T2, T3> {

    private T1 first;
    private T2 second;
    private T3 third;

    public MutableTriple(T1 first, T2 second, T3 third) {
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
        this.third = Objects.requireNonNull(third);
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
    public T3 third() {
        return third;
    }

    public void first(T1 first) {
        this.first = first;
    }

    public void second(T2 second) {
        this.second = second;
    }

    public void third(T3 third) {
        this.third = third;
    }

    @Override
    public <U1, U2, U3> MutableTriple<U1, U2, U3> map(Function<? super T1, ? extends U1> function1, Function<? super T2, ? extends U2> function2, Function<? super T3, ? extends U3> function3) {
        return new MutableTriple<>(function1.apply(first), function2.apply(second), function3.apply(third));
    }

    @Override
    public MutableTriple<T1, T2, T3> asMutableTriple() {
        return this;
    }

    @Override
    public ImmutableTriple<T1, T2, T3> asImmutableTriple() {
        return new ImmutableTriple<>(first, second, third);
    }

    @Override
    public boolean filter(TriplePredicate<T1, T2, T3> predicate) {
        return predicate.test(first, second, third);
    }
}
