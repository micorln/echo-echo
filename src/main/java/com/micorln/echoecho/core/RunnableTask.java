package com.micorln.echoecho.core;

public class RunnableTask extends TaskWrapper<Void> {

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
        if (future.hasTaskFailed()) {
            System.out.println("Task with id : " + String.valueOf(getTaskId()) + " has been cancelled. Not executing task.");
            return;
        }
        task.run();   
    }

}
