package com.micorln.echoecho.core;

public class EchoFuture<T> {

    volatile T resultValue;

    private volatile boolean hasCompleted;

    private volatile boolean hasFailed;

    public boolean hasTaskFailed() {
        return hasFailed;
    }

    public void markAsCompleted() {
        hasCompleted = true;
    }

    public boolean hasTaskCompleted() {
        return hasCompleted;
    }

    public boolean isTaskRunning() {
        return !hasCompleted && !hasFailed;
    }

    public EchoFuture() {
        
    }

    public void setResult(T result) {
        this.resultValue = result;
    }

    public T getResult() {
        return resultValue;
    }

    public T get() {

        while (!hasCompleted && !hasFailed) {


        }
        if (hasFailed) {
            throw new RuntimeException("Thread has failed");
        }
        return resultValue;

    }

    public T get(long timeLimit) {
        long timeRightNow = System.currentTimeMillis();
        while (!hasCompleted && !hasFailed) {
            if (System.currentTimeMillis() - timeRightNow >= timeLimit) {
                throw new RuntimeException("Waiting on future timed out!");
            }
        }
        if (hasFailed) {
            throw new RuntimeException("Thread has failed");
        }
        return resultValue;
    }

    public void cancel() {
        hasFailed = true;
    }

    public void complete() {
        hasCompleted = true;
    }


    
}
