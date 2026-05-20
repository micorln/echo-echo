package com.micorln.echoecho.core;

/*
 * Class that represents a worker thread.
 * A thread pool will consist of such workers
 * Requirements : 
    * Pull tasks from task queue and execute them
    * Should be able to stop gracefully when the thread pool is shutting down
    * Should handle exceptions thrown by tasks and not let them crash the worker thread
 */

public class Worker implements Runnable {

    TaskQueue taskQueue;
    private int index;
    private Thread thread;
    private volatile WorkerState workerState;

    public Worker(TaskQueue taskQueue, int index) {
        this.taskQueue = taskQueue;
        this.index = index;
        thread = new Thread(this);
        workerState = WorkerState.IDLE;
    }

    @Override
    public void run() {
        while (true && !thread.isInterrupted()) {
            try {
                Runnable task = taskQueue.pollTask();
                if (task == null) {
                    break;
                }
                workerState = WorkerState.RUNNING;
                System.out.println("Thread " + String.valueOf(index) + " : Assigned a task!");
                executeTask(task);
            } catch(InterruptedException e) {
                System.out.println("Thread " + String.valueOf(index) + " : Task was interrupted!");
            }
        }
        if (thread.isInterrupted()) {
            System.out.println("Thread " + String.valueOf(index) + " : was interrupted!");
            workerState = WorkerState.INTERRUPTED;
        } else {
            workerState = WorkerState.STOPPED;
        }
        
        System.out.println("Thread " + String.valueOf(index) + " : Bye friend. I rest now.");
    }

    private void executeTask(Runnable task) {
        try {
            task.run();
        } catch (Throwable e) {
            // Handle exceptions thrown by tasks to prevent worker thread from crashing
            System.err.println("Task threw an exception: " + e.getMessage());
        }
    }
    
    public void join(long timeoutMillis) throws InterruptedException {
        thread.join(timeoutMillis);
    }

    public int getIndex() {
        return index;
    }

    public boolean isThreadAlive() {
        return thread.isAlive();
    }

    public void interrupt() {
        thread.interrupt();
    }

    public void start() {
        thread.start();
    }

}
