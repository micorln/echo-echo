package com.micorln.echoecho.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


/*
Base class for the EchoEcho thread pool implementation. 
This class will contain the core logic for managing the thread pool, including task submission, worker management, and shutdown procedures.
Requirements:
    * Should allow clients to submit tasks to the thread pool for execution
    * Should manage a pool of worker threads that execute the submitted tasks
    * Should provide a way to gracefully shut down the thread pool, ensuring that all submitted tasks are completed before shutdown
    * Should manage task queuing.
    * Needs to maintain state in order to run, stop, shutdown (interrupt), etc.
*/

public class EchoEcho {

    int threadPoolSize;
    TaskQueue taskQueue;
    List<Worker> workers;
    private volatile PoolState poolState;
    AtomicInteger taskIds;

    public EchoEcho(int threadPoolSize) {
        workers = new ArrayList<>();
        this.threadPoolSize = threadPoolSize;
        this.taskQueue = new TaskQueue();
        this.poolState = PoolState.IDLE;
        this.taskIds = new AtomicInteger(0);
    }

    public synchronized void submit(Runnable task) {
        checkPoolState();
        RunnableTask newRunnableTask = new RunnableTask(task, taskIds.incrementAndGet());
        taskQueue.submit(newRunnableTask);
        if (workers.size() < threadPoolSize && taskQueue.size() > 0) {
            Worker newGuy = new Worker(taskQueue, workers.size() + 1);
            newGuy.start();
            workers.add(newGuy);
            if (workers.size() == 1) {
                poolState = PoolState.RUNNING;
            }
        }
    }

    public synchronized <T> EchoFuture<T> submit(Callable<T> task) {
        checkPoolState();
        CallableTask<T> newCallableTask = new CallableTask<T>(task, taskIds.incrementAndGet());
        taskQueue.submit(newCallableTask);
        
        if (workers.size() < threadPoolSize && taskQueue.size() > 0) {
            Worker newGuy = new Worker(taskQueue, workers.size() + 1);
            newGuy.start();
            workers.add(newGuy);
            if (workers.size() == 1) {
                poolState = PoolState.RUNNING;
            }
        }
        return newCallableTask.getFuture();

    }

    private synchronized void checkPoolState() {
        if (!poolState.equals(PoolState.IDLE) && !poolState.equals(PoolState.RUNNING)) {
            System.out.println(poolState);
            throw new RuntimeException("EchoEcho has been shut down, no more tasks are allowed!");
        }
    }

    public synchronized void shutdown() {
        poolState = PoolState.SHUTTING_DOWN;
        taskQueue.shutdown();
    }

    public synchronized void awaitTermination(long timeoutMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        for (Worker w : workers) {
            if (System.currentTimeMillis() >= deadline) {
                break;
            }
            try {
                w.join(timeoutMillis);
            } catch (InterruptedException e) {
                System.out.println("Thread " + w.getIndex() + " was interrupted!");
            }
        }
        for (Worker w : workers) {
            if (w.isThreadAlive()) {
                System.out.println("Rest now brother " + w.getIndex());
                w.interrupt();
            }
        }
        poolState = PoolState.STOPPED;
    }

}
