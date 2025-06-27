package com.gurula.stockMate.exception;

public class Result<T, E> {
    private final T ok;
    private final E err;
    private final boolean isOk;

    private Result(T ok, E err, boolean isOk) {
        this.ok = ok;
        this.err = err;
        this.isOk = isOk;
    }

    public static <T, E> Result<T, E> ok(T value) {
        return new Result<>(value, null, true);
    }

    public static <T, E> Result<T, E> err(E error) {
        return new Result<>(null, error, false);
    }

    public boolean isOk() {
        return isOk;
    }

    public boolean isErr() {
        return !isOk;
    }

    public T unwrap() {
        if (!isOk) throw new IllegalStateException("Tried to unwrap Err");
        return ok;
    }

    public E unwrapErr() {
        if (isOk) throw new IllegalStateException("Tried to unwrap Ok");
        return err;
    }
}
