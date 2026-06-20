package com.micorln.echoecho.benchmarking;

import java.util.ArrayList;

public class SubmitThroughput extends Benchmarker {
    
    ArrayList<Runnable> tasks;

    public SubmitThroughput(int numThreads) {
        super(numThreads);
        tasks = new ArrayList<>();
    }

    public void createTasks(int numTasks) {
        for (int i = 0; i < numTasks; i++) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
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
        SubmitThroughput submitThroughput = new SubmitThroughput(4);
        submitThroughput.createTasks(100000);

        long startTimeEchoEcho = System.currentTimeMillis();

        for (Runnable task : submitThroughput.tasks) {
            submitThroughput.submit(task);
        }

        System.out.println("EchoEcho time taken : " + String.valueOf(System.currentTimeMillis() - startTimeEchoEcho) + " ms");


        submitThroughput.threadPool.shutdown();
        submitThroughput.threadPool.awaitTermination(500000L);

        

        long startTimeJava = System.currentTimeMillis();

        for (Runnable task : submitThroughput.tasks) {
            submitThroughput.javaSubmit(task);
        }

        System.out.println("Java time taken : " + String.valueOf(System.currentTimeMillis() - startTimeJava) + " ms");

        submitThroughput.scheduler.shutdown();
        try {
            submitThroughput.scheduler.awaitTermination(500000L, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
