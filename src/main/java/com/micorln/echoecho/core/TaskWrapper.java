package com.micorln.echoecho.core;

public abstract class TaskWrapper implements Runnable {

    private long taskId;

    private long submissionTime;

    private long executionStartTime;

    private long executionEndTime;

    public TaskWrapper(long taskId) {
        this.taskId = taskId;
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

}
