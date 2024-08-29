package de.aethos.lib.tuple.pair;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

public sealed interface Pair<T1, T2> permits ImmutablePair, MutablePair {

    <U> Pair<U, T2> mapFirst(Function<? super T1, ? extends U> function);

    <U> Pair<T1, U> mapSecond(Function<? super T2, ? extends U> function);
    
    <U1, U2> Pair<U1, U2> map(Function<? super T1, ? extends U1> function1, Function<? super T2, ? extends U2> function2);

    T1 first();

    T2 second();

    Pair<T2, T1> change();

    MutablePair<T1, T2> asMutablePair();

    ImmutablePair<T1, T2> asImmutablePair();

    default <U> U map(BiFunction<? super T1, ? super T2, ? extends U> function) {
        return function.apply(first(), second());
    }

    default Map.Entry<T1, T2> asEntry() {
        return Map.entry(first(), second());
    }

    default boolean filter(BiPredicate<? super T1, ? super T2> predicate) {
        return predicate.test(first(), second());
    }


}
