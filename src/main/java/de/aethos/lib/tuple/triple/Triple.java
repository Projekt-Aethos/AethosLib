package de.aethos.lib.tuple.triple;

import java.util.function.Function;

public sealed interface Triple<T1, T2, T3> permits ImmutableTriple, MutableTriple {


    T1 first();

    T2 second();

    T3 third();
    
    <U1, U2, U3> Triple<U1, U2, U3> map(Function<? super T1, ? extends U1> function1, Function<? super T2, ? extends U2> function2, Function<? super T3, ? extends U3> function3);

    default <U> U map(TripleFunction<? super T1, ? super T2, ? super T3, ? extends U> function) {
        return function.apply(first(), second(), third());
    }

    MutableTriple<T1, T2, T3> asMutableTriple();

    ImmutableTriple<T1, T2, T3> asImmutableTriple();

    boolean filter(TriplePredicate<T1, T2, T3> predicate);

    interface TriplePredicate<T1, T2, T3> {
        boolean test(T1 first, T2 second, T3 third);
    }

    interface TripleFunction<T1, T2, T3, U> {
        U apply(T1 first, T2 second, T3 third);
    }

    final class Utils {

    }
}
