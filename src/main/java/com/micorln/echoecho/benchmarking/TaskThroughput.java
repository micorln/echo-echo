package com.micorln.echoecho.benchmarking;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class TaskThroughput extends Benchmarker {

    ArrayList<Runnable> tasks;

    public TaskThroughput(int numThreads) {
        super(numThreads);
        tasks = new ArrayList<>();
    }

    public void createTasks(int numTasks) {
        // AtomicLong sink = new AtomicLong();
        for (int i = 0; i < numTasks; i++) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    // long sum = 0;
                    // for (int i = 0; i < 100_000; i++) {
                    //     sum += i;
                    // }
                    // sink.addAndGet(sum);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void main(String[] args) {
        TaskThroughput taskThroughput = new TaskThroughput(1);
        taskThroughput.createTasks(1_000_000);


        long startTimeEchoEcho = System.currentTimeMillis();

        for (Runnable task : taskThroughput.tasks) {
            taskThroughput.submit(task);
        }

        taskThroughput.threadPool.shutdown();
        taskThroughput.threadPool.awaitTermination(500000L);

        System.out.println("EchoEcho time taken : " + String.valueOf(System.currentTimeMillis() - startTimeEchoEcho) + " ms");



        long startTimeJava = System.currentTimeMillis();

        for (Runnable task : taskThroughput.tasks) {
            taskThroughput.javaSubmit(task);
        }

        taskThroughput.scheduler.shutdown();
        try {
            taskThroughput.scheduler.awaitTermination(500000L, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Java time taken : " + String.valueOf(System.currentTimeMillis() - startTimeJava) + " ms");
    
        

    }
    
}
