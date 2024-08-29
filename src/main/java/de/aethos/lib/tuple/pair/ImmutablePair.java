package de.aethos.lib.tuple.pair;

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public record ImmutablePair<T1, T2>(T1 first, T2 second) implements Pair<T1, T2> {

    public ImmutablePair {
        Preconditions.checkArgument(first != null && second != null);
    }

    @Override
    public <U> U map(BiFunction<? super T1, ? super T2, ? extends U> function) {
        return function.apply(first, second);
    }

    @Override
    public <U> ImmutablePair<U, T2> mapFirst(Function<? super T1, ? extends U> function) {
        return new ImmutablePair<>(function.apply(first), second);
    }

    @Override
    public <U> ImmutablePair<T1, U> mapSecond(Function<? super T2, ? extends U> function) {
        return new ImmutablePair<>(first, function.apply(second));
    }

    @Override
    public Map.Entry<T1, T2> asEntry() {
        return Map.entry(first, second);
    }

    @Override
    public MutablePair<T1, T2> asMutablePair() {
        return new MutablePair<>(first, second);
    }

    @Override
    public ImmutablePair<T1, T2> asImmutablePair() {
        return this;
    }

    @Override
    public boolean filter(BiPredicate<? super T1, ? super T2> predicate) {
        return predicate.test(first, second);
    }


    @Override
    public <U1, U2> Pair<U1, U2> map(Function<? super T1, ? extends U1> function1, Function<? super T2, ? extends U2> function2) {
        return new ImmutablePair<>(function1.apply(first), function2.apply(second));
    }

    @Override
    public Pair<T2, T1> change() {
        return new ImmutablePair<>(second, first);
    }


}
