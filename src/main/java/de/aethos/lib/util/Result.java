package de.aethos.lib.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;


public class Result<V, E> {
    private final V ok;
    private final E error;
    private final State state;

    private Result(V ok, E error, State state) {
        this.ok = ok;
        this.error = error;
        this.state = state;
    }

    public static <V,E> Result<V,E> ok(V v){
        return new Result<>(v,null,State.OK);
    }

    public static <V,E> Result<V,E> error(E e){
        return new Result<>(null,e,State.ERROR);
    }

    public static <V> Result<V,NullPointerException> fromOpt(Optional<V> opt){
        if (opt.isPresent()){
            return Result.ok(opt.get());
        } else {
            return Result.error(new NullPointerException());
        }
    }

    public Optional<V> getValue(){
        if (state.equals(State.OK)){
            return Optional.of(this.ok);
        }
        return Optional.empty();
    }
    public Optional<E> getError(){
        if (state.equals(State.ERROR)){
            return Optional.of(error);
        }
        return Optional.empty();
    }

    public <R> R map(Function<V,R> func,R r ){
        if (this.state.equals(State.OK)){
            return func.apply(this.ok);
        } else {
            return r;
        }
    }

    public void match(Consumer<V> vConsumer, Consumer<E> eConsumer){
        if (state.equals(State.OK)){
            vConsumer.accept(ok);
        } else {
            eConsumer.accept(error);
        }
    }

    public V unwrap() throws IllegalStateException{
        if (state.equals(State.OK)){
            return ok;
        }
        throw new IllegalStateException("Try to unwrap an error Result");
    }


    private enum State{
        OK,
        ERROR;
    }




}
