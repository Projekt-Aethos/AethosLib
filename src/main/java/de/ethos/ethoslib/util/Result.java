package de.ethos.ethoslib.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class Result <V,R>{
    private final V value;

    private final R reason;

    private final Exception exception;

    private final ResultStatus status;


    private Result(V value, R reason, Exception exception, ResultStatus status){
        this.value = value;
        this.reason = reason;
        this.exception = exception;
        this.status = status;
    }

    public static <V,R> Result<V,R> of(Optional<V> optional){
        try{
            return Result.ok(optional.get());
        } catch (Exception e){
            return Result.error(e);
        }
    }

    public static <V,R> Result<V,R> of(Callable<Result<V,R>> callable){
        try {
            return callable.call();
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    public static <V,R> Result<V,R> ok(V value){
        if (value != null){
            return new Result<>(value, null, null, ResultStatus.VALUE);
        }
        return Result.error(new NullPointerException());
    }

    public static  <V,R> Result<V,R> reason(R reason){
        return new Result<>(null,reason,null,ResultStatus.REASON);
    }

    public static <V,R> Result<V,R> error(Exception reason){
        return new Result<>(null, null, reason, ResultStatus.ERROR);
    }

    public boolean isOk(){
        return value != null && !hasReason();
    }


    public void ifPresent(Consumer<V> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    public void ifPresentOrElse(Consumer<V> valueCon, Consumer<Exception> reasonCon){
        if (isOk()){
            valueCon.accept(value);
        } else {
            reasonCon.accept(exception);
        }
    }


    public @NotNull V getOrElseThrow(){
        if (value == null){
            if (exception instanceof RuntimeException exception){
                throw exception;
            } else {
                throw new RuntimeException(exception);
            }
        } else {
            return value;
        }
    }

    public @NotNull V getOrDefault(@NotNull V def){
        if (value == null){
            return def;
        } else {
            return value;
        }
    }


    public Optional<V> asOptional(){
        return Optional.of(value);
    }


    public Result<V,R> filter(Predicate<? super V> predicate){
        if (predicate == null){
            return Result.error(new NullPointerException());
        }
        try{
            if (isOk() && predicate.test(value)){
                return this;
            } else {
                return Result.error(new NullPointerException("Value not present"));
            }
        } catch (Exception e){
            return Result.error(e);
        }
    }

    public <T> T map(Function<V,T> methode){
        return methode.apply(value);
    }

    public boolean hasReason(){
        return exception != null;
    }

    public R getReasonOrElseThrow(){
        if (reason != null) {
            return reason;
        }
        throw new RuntimeException(exception);
    }
    public <T> Result<T,Exception> getField(Function<V,T> methode){
        if (value == null) {
            return Result.reason(exception);
        } else {
            try{
                final T t = methode.apply(value);
                if (t == null){
                    return Result.reason(new RuntimeException("Methode call returns null"));
                }
                return Result.ok(t);
            } catch (Exception e){
                return Result.error(e);
            }
        }
    }

    public ResultStatus getStatus() {
        return status;
    }



}
