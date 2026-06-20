package com.micorln.echoecho.core;

import java.util.Comparator;
import java.util.PriorityQueue;

/*
Class to represent the task queue for the thread pool. This will be a shared resource that worker threads will pull tasks from and clients will submit tasks to.
Will be a class attribute of the EchoEcho class.
Requirements:
    * Should allow clients to submit tasks to the queue for execution by worker threads
    * Should allow worker threads to pull tasks from the queue for execution
    * Should handle synchronization to ensure thread safety when multiple threads are accessing the queue concurrently
*/

public class TaskQueue <T> {
    
    PriorityQueue<T> taskQueue;

    volatile boolean open = true;

    public TaskQueue(Comparator<T> comparator) {
        this.taskQueue = new PriorityQueue<>(comparator);
    }

    public synchronized void shutdown() {
        open = false;
        notify();
    }

    public synchronized void submit(T task) {
        if (!open) {
            throw new IllegalStateException("Cannot submit task to a closed TaskQueue!");
        }
        taskQueue.add(task);
        notify();
    }

    public synchronized T peek() {
        return taskQueue.peek();
    }

    public synchronized T pollTask() throws InterruptedException {
        while (taskQueue.size() == 0) {
            if (!open) {
                return null;
            }
            wait();
        }

        T topTask = taskQueue.poll();
        notify();
        return topTask;
    }

    public synchronized T pollTask(long timeToWait) throws InterruptedException {
        if (taskQueue.size() == 0) {
            if (!open) {
                return null;
            }
            wait(timeToWait);
        }

        if (taskQueue.size() == 0) {
            return null;
        }

        T topTask = taskQueue.poll();
        notify();
        return topTask;
    }

    public int size() {
        return taskQueue.size();
    }

}
