package de.aethos.lib.tuple.triple;

import com.google.common.base.Preconditions;

import java.util.function.Function;

public record ImmutableTriple<T1, T2, T3>(T1 first, T2 second, T3 third) implements Triple<T1, T2, T3> {
    public ImmutableTriple {
        Preconditions.checkArgument(first != null && second != null && third != null);
    }

    @Override
    public <U1, U2, U3> Triple<U1, U2, U3> map(Function<? super T1, ? extends U1> function1, Function<? super T2, ? extends U2> function2, Function<? super T3, ? extends U3> function3) {
        return new ImmutableTriple<>(function1.apply(first), function2.apply(second), function3.apply(third));
    }

    @Override
    public MutableTriple<T1, T2, T3> asMutableTriple() {
        return new MutableTriple<>(first, second, third);
    }

    @Override
    public ImmutableTriple<T1, T2, T3> asImmutableTriple() {
        return this;
    }

    @Override
    public boolean filter(TriplePredicate<T1, T2, T3> predicate) {
        return predicate.test(first, second, third);
    }
}
