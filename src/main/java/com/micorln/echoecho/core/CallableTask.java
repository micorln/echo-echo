package com.micorln.echoecho.core;

import java.util.concurrent.Callable;

public class CallableTask<T> extends TaskWrapper<T> {

    private T result;

    private Callable<T> task;

    public T getResult() {
        return result;
    }

    public synchronized void run() {
        try {
            if (!future.hasTaskFailed()) {
                future.setResult(task.call());
                future.complete();
            } else {
                System.out.println("Task with id : " + String.valueOf(getTaskId()) + " has been cancelled. Not executing task.");
            }
        
        } catch (Exception ex) {
            future.cancel();
            throw new RuntimeException(ex);
        }
    }

    public CallableTask(Callable<T> callable, long taskId) {
        super(taskId);
        this.task = callable;
        this.future = new EchoFuture<T>();
    }

    public CallableTask(Callable<T> callable, long taskId, long priority) {
        super(taskId, priority);
        this.task = callable;
        this.future = new EchoFuture<T>();
    }

}
