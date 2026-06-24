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

    volatile boolean closed = false;

    public TaskQueue(Comparator<T> comparator) {
        this.taskQueue = new PriorityQueue<>(comparator);
    }

    public synchronized void shutdown() {
        open = false;
        // closed = true;
        notifyAll();
    }

    public synchronized void close() {
        open = false;
        closed = true;
        notifyAll();
    }

    public synchronized void submit(T task) {
        if (!open) {
            throw new IllegalStateException("Cannot submit task to a closed TaskQueue!");
        }
        taskQueue.add(task);
        notifyAll();
    }

    public synchronized T peek() {
        return taskQueue.peek();
    }

    public synchronized T pollTask() throws InterruptedException {
        while (taskQueue.size() == 0) {
            if (closed) {
                System.out.println("Returning null from pollTask() as the task queue is closed and empty -1!");
                return null;
            }
            wait();
        }

        T topTask = taskQueue.poll();
        notifyAll();
        return topTask;
    }

    public synchronized T pollTask(long timeToWait) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (taskQueue.size() == 0) {
            if (closed) {
                // System.out.println("Returning null from pollTask() as the task queue is closed and empty -2!");
                return null;
            }
            wait(timeToWait);
            if (System.currentTimeMillis() - startTime >= timeToWait) {
                break;
            }
        }

        if (taskQueue.size() == 0) {
            // System.out.println("[pollTask] Timeout expired (waited " + (System.currentTimeMillis() - startTime) + "ms, timeout=" + timeToWait + "), queue empty, returning null");
            return null;
        }

        T topTask = taskQueue.poll();
        // if (topTask == null) {
            // System.out.println("[pollTask] Returning null - queue.poll() returned null");
        // }
        notifyAll();
        return topTask;
    }

    public int size() {
        return taskQueue.size();
    }

}
