package com.micorln.echoecho.core;

public abstract class TaskWrapper<T> implements Runnable {

    private long taskId;

    private long submissionTime;

    private long executionStartTime;

    private long executionEndTime;

    private long priority;

    protected EchoFuture<T> future;

    public TaskWrapper(long taskId) {
        this.taskId = taskId;
        submissionTime = System.currentTimeMillis();
        this.priority = 0;
        this.future = new EchoFuture<T>();
    }

    public TaskWrapper(long taskId, long priority) {
        this.taskId = taskId;
        this.priority = priority;
        this.future = new EchoFuture<T>();
        submissionTime = System.currentTimeMillis();
    }

    public long getTaskId() {
        return taskId;
    }
    
    public long getSubmissionTime() {
        return submissionTime;
    }

    public long getExecutionStartTime() {
        return executionStartTime;
    }

    public long setExecutionStartTime() {
        this.executionStartTime = System.currentTimeMillis();
        return executionStartTime;
    }

    public long getExecutionEndTime() {
        return executionEndTime;
    }

    public long setExecutionEndTime() {
        this.executionEndTime = System.currentTimeMillis();
        return executionEndTime;
    }

    public long getPriority() {
        return priority;
    }

    public EchoFuture<T> getFuture() {
        return future;
    }

    public void setFuture(EchoFuture<T> future) {
        this.future = future;
    }

}
