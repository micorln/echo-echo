package com.micorln.echoecho.benchmarking;

import java.util.concurrent.ScheduledExecutorService;
import com.micorln.echoecho.core.EchoEcho;

/*
 * Setup class that will be inherited by each bencherking class. It will provide the basic structure for benchmarking.
*/
public class Benchmarker<T> {

    EchoEcho threadPool;

    ScheduledExecutorService scheduler;

    public Benchmarker(int numThreads) {
        this.threadPool = new EchoEcho(numThreads, false);
        this.scheduler = java.util.concurrent.Executors.newScheduledThreadPool(numThreads);
    }

    public void submit(Runnable task) {
        threadPool.submit(task);
    }

    public void javaSubmit(Runnable task) {
        scheduler.submit(task);
    }
    
}
