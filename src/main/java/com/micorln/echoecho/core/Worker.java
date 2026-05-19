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
    int index;
    Thread thread;
    volatile String workerState;

    public Worker(TaskQueue taskQueue, int index) {
        this.taskQueue = taskQueue;
        this.index = index;
        thread = new Thread(this);
        workerState = "IDLE";
    }

    public void start() {
        workerState = "RUNNING";
        thread.start();
    }

    @Override
    public void run() {
        while (!workerState.equals("STOPPING")) {
            try {
                Runnable task = taskQueue.pollTask();
                if (task == null) {
                    break;
                }
                System.out.println("Thread " + String.valueOf(index) + " : Assigned a task!");
                executeTask(task);
            } catch(InterruptedException e) {
                System.out.println("Thread " + String.valueOf(index) + " : Task was interrupted!");
            }
        }
        System.out.println("Thread " + String.valueOf(index) + " : I rest now. Bye!");
    }

    private void executeTask(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            // Handle exceptions thrown by tasks to prevent worker thread from crashing
            System.err.println("Task threw an exception: " + e.getMessage());
        }
    }

    public void shutdown() {
        workerState = "STOPPING";
    }
    
    public void join() throws InterruptedException {
        thread.join();
    }
}
