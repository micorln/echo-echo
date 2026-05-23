package com.micorln.echoecho.core;

import java.util.LinkedList;
import java.util.Queue;

/*
Class to represent the task queue for the thread pool. This will be a shared resource that worker threads will pull tasks from and clients will submit tasks to.
Will be a class attribute of the EchoEcho class.
Requirements:
    * Should allow clients to submit tasks to the queue for execution by worker threads
    * Should allow worker threads to pull tasks from the queue for execution
    * Should handle synchronization to ensure thread safety when multiple threads are accessing the queue concurrently
*/

public class TaskQueue {

    Queue<TaskWrapper> taskQueue;
    volatile boolean open = true;

    public TaskQueue() {
        this.taskQueue = new LinkedList<TaskWrapper>();
    }

    public synchronized void shutdown() {
        open = false;
        notifyAll();
    }

    public synchronized void submit(TaskWrapper task) {
        taskQueue.add(task);
        notifyAll();
    }

    public synchronized TaskWrapper pollTask() throws InterruptedException {
        while (taskQueue.size() == 0 && open) {
            wait();
        }
        if (!open) {
            return null;
        }

        TaskWrapper topTask = taskQueue.poll();
        notifyAll();
        return topTask;
    }

    public int size() {
        return taskQueue.size();
    }

}
