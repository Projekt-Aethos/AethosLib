package de.aethos.lib.tuple;

import de.aethos.lib.tuple.pair.ImmutablePair;
import de.aethos.lib.tuple.pair.Pair;
import de.aethos.lib.tuple.triple.ImmutableTriple;
import de.aethos.lib.tuple.triple.Triple;

import java.util.Map;
import java.util.function.Function;

public interface Tuple {

    static <T1, T2> Pair<T1, T2> of(T1 t1, T2 t2) {
        return new ImmutablePair<>(t1, t2);
    }

    static <T1, T2> Pair<T1, T2> of(Map.Entry<T1, T2> entry) {
        return new ImmutablePair<>(entry.getKey(), entry.getValue());
    }

    static <T1, T2, T3> Triple<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
        return new ImmutableTriple<>(t1, t2, t3);
    }

    static <T, U> Pair<U, U> of(Pair<T, T> pair, Function<T, U> function) {
        return pair.map(function, function);
    }

    static void main(String[] args) {
        Integer integer = Tuple.of(10, 10, 10).map((first, second, third) -> third);
    }

}
