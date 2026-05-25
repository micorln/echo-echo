package com.micorln.echoecho.core;

import java.util.PriorityQueue;

/*
Class to represent the task queue for the thread pool. This will be a shared resource that worker threads will pull tasks from and clients will submit tasks to.
Will be a class attribute of the EchoEcho class.
Requirements:
    * Should allow clients to submit tasks to the queue for execution by worker threads
    * Should allow worker threads to pull tasks from the queue for execution
    * Should handle synchronization to ensure thread safety when multiple threads are accessing the queue concurrently
*/

public class TaskQueue {

    PriorityQueue<TaskWrapper> taskQueue;

    volatile boolean open = true;

    public TaskQueue() {
        this.taskQueue = new PriorityQueue<>(
            (t1, t2) -> Long.compare(t2.getPriority(), t1.getPriority())
        );
    }

    public synchronized void shutdown() {
        open = false;
        notifyAll();
    }

    public synchronized void submit(TaskWrapper task) {
        if (!open) {
            throw new IllegalStateException("Cannot submit task to a closed TaskQueue!");
        }
        taskQueue.add(task);
        notifyAll();
    }

    public synchronized TaskWrapper pollTask() throws InterruptedException {
        while (taskQueue.size() == 0) {
            if (!open) {
                return null;
            }
            wait();
        }

        TaskWrapper topTask = taskQueue.poll();
        notifyAll();
        return topTask;
    }

    public synchronized TaskWrapper pollTask(long timeToWait) throws InterruptedException {
        if (taskQueue.size() == 0) {
            if (!open) {
                return null;
            }
            wait(timeToWait);
        }

        if (taskQueue.size() == 0) {
            return null;
        }

        TaskWrapper topTask = taskQueue.poll();
        notifyAll();
        return topTask;
    }

    public synchronized TaskWrapper pollTask(Worker worker) throws InterruptedException {
        while (taskQueue.size() == 0) {
            if (!open) {
                return null;
            }
            wait();
        }
        if (worker.getWorkerState() != WorkerState.IDLE) {
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
