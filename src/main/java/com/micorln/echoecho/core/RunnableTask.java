package com.micorln.echoecho.core;

public class RunnableTask extends TaskWrapper {

    Runnable task;

    public RunnableTask(Runnable task, long taskId) {
        super(taskId);
        this.task = task;
    }

    public RunnableTask(Runnable task, long taskId, long priority) {
        super(taskId, priority);
        this.task = task;
    }

    public void run() {
        task.run();   
    }

}
