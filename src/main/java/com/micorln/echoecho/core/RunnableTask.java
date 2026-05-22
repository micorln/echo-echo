package com.micorln.echoecho.core;

public class RunnableTask implements TaskWrapper {

    Runnable task;

    public RunnableTask(Runnable task) {
        this.task = task;
    }

    public void run() {
        task.run();   
    }

}
