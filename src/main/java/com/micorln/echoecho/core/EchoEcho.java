package com.micorln.echoecho.core;

import java.util.ArrayList;
import java.util.List;


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
    public volatile String threadPoolStatus;

    public EchoEcho(int threadPoolSize) {
        workers = new ArrayList<>();
        this.threadPoolSize = threadPoolSize;
        this.taskQueue = new TaskQueue();
        threadPoolStatus = "STARTED";
    }

    public void submit(Runnable task) {
        taskQueue.submit(task);
        if (workers.size() < threadPoolSize && taskQueue.size() > 0) {
            Worker newGuy = new Worker(taskQueue, workers.size() + 1);
            newGuy.start();
            workers.add(newGuy);
        }
    }

    public void shutdown() {
        threadPoolStatus = "SHUTDOWN";
        taskQueue.shutdown();
        for (Worker w : workers) {
            try {
                w.shutdown();
                w.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    
}
