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
                TaskWrapper task = taskQueue.pollTask();
                if (task == null) {
                    break;
                }
                workerState = WorkerState.RUNNING;
                System.out.println("Thread " + String.valueOf(index) + " : Assigned task : " + String.valueOf(task.getTaskId()) + "!");
                task.setExecutionStartTime();
                try {
                    task.run();
                } catch(Exception e) {
                    throw new RuntimeException("Thread " + String.valueOf(index) + " : Task" + String.valueOf(task.getTaskId()) + 
                    "threw an exception! " + e.getMessage());
                }
                task.setExecutionEndTime();
                System.out.println("Thread " + String.valueOf(index) + " : Completed task : " + String.valueOf(task.getTaskId()) + " in " 
                        + String.valueOf(task.getExecutionEndTime() - task.getExecutionStartTime()) + " ms!");
            } catch(InterruptedException e) {
                System.out.println("Thread " + String.valueOf(index) + " : Task was interrupted!");
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        if (thread.isInterrupted()) {
            System.out.println("Thread " + String.valueOf(index) + " : was interrupted!");
            workerState = WorkerState.INTERRUPTED;
        } else {
            workerState = WorkerState.COMPLETED;
        }
        
        System.out.println("Thread " + String.valueOf(index) + " : Bye friend. I rest now.");
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

    public WorkerState getWorkerState() {
        return workerState;
    }

}
