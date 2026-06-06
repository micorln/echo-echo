package com.micorln.echoecho.core;

public class ScheduledTask<T> {

    private long nextExecutionTime;

    private TaskWrapper<T> task;

    private long delay;

    public ScheduledTask(TaskWrapper<T> taskWrapper, long nextExecutionTime, long delay) {
        this.task = taskWrapper;
        this.nextExecutionTime = nextExecutionTime;
        this.delay = delay;
    }

    public void setNextExecutionTime(long nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
    }

    public long getNextExecutionTime() {
        return nextExecutionTime;
    }

    public TaskWrapper<T> getTask() {
        return task;
    }

    public void setFuture(EchoFuture<T> future) {
        task.setFuture(future);
    }

    public long getDelay() {
        return delay;
    }
}
