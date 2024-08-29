package de.aethos.lib.tuple.triple;

import java.util.function.Function;

public interface Triple<T1, T2, T3> {


    T1 first();

    T2 second();

    T3 third();


    <U1, U2, U3> Triple<U1, U2, U3> map(Function<? super T1, ? extends U1> function1, Function<? super T2, ? extends U2> function2, Function<? super T3, ? extends U3> function3);

}
