package com.micorln.echoecho.core;

import java.util.concurrent.Callable;

public class CallableTask<T> implements TaskWrapper {

    private T result;

    private Callable<T> task;

    private EchoFuture<T> future;

    public T getResult() {
        return result;
    }

    public EchoFuture<T> getFuture() {
        return future;
    }

    public void run() {
        try {
            future.setResult(task.call());
            future.complete();
        } catch (Exception ex) {
            future.cancel();
            throw new RuntimeException(ex);
        }
    }

    public CallableTask(Callable<T> callable) {
        this.task = callable;
        this.future = new EchoFuture<T>();
    }

}
