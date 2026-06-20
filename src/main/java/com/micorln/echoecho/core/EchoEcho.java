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

/*
 * Pending features:
 * 1. Scheduled execution
 * 2. Dynamic worker management
 * 3. Benchmarking ?
*/

public class EchoEcho {

    final int threadPoolSize;
    TaskQueue<TaskWrapper<?>> taskQueue;
    List<Worker> workers;
    private volatile PoolState poolState;
    AtomicInteger taskIds;
    private final long TIME_TO_WAIT = 100L;
    private final boolean DEBUG;

    private Object submitLock = new Object();
    private Object pollLock = new Object();

    public EchoEcho(int threadPoolSize) {
        workers = new ArrayList<>();
        this.threadPoolSize = threadPoolSize;
        this.taskQueue = new TaskQueue<TaskWrapper<?>>((t1, t2) -> Long.compare(t2.getPriority(), t1.getPriority()));
        this.poolState = PoolState.IDLE;
        this.taskIds = new AtomicInteger(0);
        this.DEBUG = false;
        startWorkerManager();
    }

    public EchoEcho(int threadPoolSize, boolean debug) {
        workers = new ArrayList<>();
        this.threadPoolSize = Runtime.getRuntime().availableProcessors();
        this.taskQueue = new TaskQueue<TaskWrapper<?>>((t1, t2) -> Long.compare(t2.getPriority(), t1.getPriority()));
        this.poolState = PoolState.IDLE;
        this.taskIds = new AtomicInteger(0);
        this.DEBUG = debug;
        startWorkerManager();
    }

    public synchronized int countWorkers() {
        int aliveWorkers = 0;
        for (Worker w : workers) {
            if (w.isThreadAlive()) {
                aliveWorkers++;
            }
        }
        return aliveWorkers;
    }


    public EchoFuture<Void> submit(Runnable task) {
        
        if (DEBUG) {
            System.out.println("Current worker count : " + String.valueOf(countWorkers()));
        }
        checkPoolState();
        RunnableTask newRunnableTask = new RunnableTask(task, taskIds.incrementAndGet());
        taskQueue.submit(newRunnableTask);
        // notifyAll();
        // manageWorkers();
        return newRunnableTask.getFuture();
        
    }
    
    public synchronized EchoFuture<Void> submit(Runnable task, long priority) {
        if (DEBUG) {
            System.out.println("Current worker count : " + String.valueOf(countWorkers()));
        }

        checkPoolState();
        RunnableTask newRunnableTask = new RunnableTask(task, taskIds.incrementAndGet(), priority);
        taskQueue.submit(newRunnableTask);
        notifyAll();
        // manageWorkers();
        return newRunnableTask.getFuture();
    }

    public synchronized <T> EchoFuture<T> submit(Callable<T> task) {
        checkPoolState();
        CallableTask<T> newCallableTask = new CallableTask<T>(task, taskIds.incrementAndGet());
        taskQueue.submit(newCallableTask);
        // manageWorkers();
        notifyAll();
        return newCallableTask.getFuture();
    }

    public synchronized <T> EchoFuture<T> submit(TaskWrapper<T> task) {
        if (DEBUG) {
            System.out.println("Current worker count : " + String.valueOf(countWorkers()));
        }
        
        checkPoolState();
        taskQueue.submit(task);
        // manageWorkers();
        notifyAll();
        return task.getFuture();
    }

    public synchronized <T> EchoFuture<T> submit(Callable<T> task, long priority) {
        checkPoolState();
        CallableTask<T> newCallableTask = new CallableTask<T>(task, taskIds.incrementAndGet(), priority);
        taskQueue.submit(newCallableTask);
        // manageWorkers();
        notifyAll();
        return newCallableTask.getFuture();
    }

    private void startWorkerManager() {
        Thread workerManager = new Thread(() -> manageWorkers());
        workerManager.start();
    }

    private synchronized void manageWorkers() {
        
        
        while (poolState.equals(PoolState.RUNNING) || poolState.equals(PoolState.IDLE)) {
            
            while (taskQueue.size() == 0) {
                try {
                    wait();
                    // System.out.println("Managing workers... Current worker count : " + String.valueOf(countWorkers()));
                    System.out.println("Current pool state : " + String.valueOf(poolState));
                    if (!(poolState.equals(PoolState.RUNNING) || poolState.equals(PoolState.IDLE))) {
                        break;

                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (countWorkers() < threadPoolSize && taskQueue.size() > 0) {
                Worker newGuy = new Worker(taskQueue, workers.size() + 1, TIME_TO_WAIT);
                newGuy.start();
                workers.add(newGuy);
                if (workers.size() == 1) {
                    poolState = PoolState.RUNNING;
                }
            }
        }        
    }


        
    
        
        // if (countWorkers() < threadPoolSize && taskQueue.size() > 0) {
        //     Worker newGuy = new Worker(taskQueue, workers.size() + 1, TIME_TO_WAIT);
        //     newGuy.start();
        //     workers.add(newGuy);
        //     if (workers.size() == 1) {
        //         poolState = PoolState.RUNNING;
        //     }
        // }
    

    private synchronized void checkPoolState() {
        if (!poolState.equals(PoolState.IDLE) && !poolState.equals(PoolState.RUNNING)) {
            if (DEBUG) {
                System.out.println(poolState);
            }
            throw new RuntimeException("EchoEcho has been shut down, no more tasks are allowed!");
        }
    }

    public synchronized void shutdown() {
        poolState = PoolState.SHUTTING_DOWN;
        taskQueue.shutdown();
        notifyAll();
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
                if (DEBUG) {
                    System.out.println("Rest now brother " + w.getIndex());
                }
                w.interrupt();
            }
        }
        poolState = PoolState.STOPPED;
    }



}
