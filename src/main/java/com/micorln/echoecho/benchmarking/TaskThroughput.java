package com.micorln.echoecho.benchmarking;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import com.micorln.echoecho.core.EchoEcho;


public class TaskThroughput extends Benchmarker {

    ArrayList<Runnable> tasks;

    public TaskThroughput(int numThreads) {
        super(numThreads);
        tasks = new ArrayList<>();
    }

    public ArrayList<Runnable> createTasks(int numTasks) {
        tasks = new ArrayList<>();
        AtomicLong sink = new AtomicLong();
        for (int i = 0; i < numTasks; i++) {
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    long sum = 0;
                    for (int i = 0; i < 100_000; i++) {
                        sum += i;
                    }
                    sink.addAndGet(sum);
                    
                }
            });
        }
        return tasks;
    }

    public static void main(String[] args) {
        TaskThroughput taskThroughput = new TaskThroughput(20);
        int threads = 20;
        ArrayList<Runnable> tasks = taskThroughput.createTasks(1_000_000);
        boolean runJava = true;
        boolean runEchoEcho = true;

        if (runJava) {

            long startTimeJava = System.currentTimeMillis();
            ExecutorService scheduler = Executors.newFixedThreadPool(threads);
            for (Runnable task : tasks) {
                scheduler.submit(task);
            }

            // System.out.println("Java submit time taken : " + String.valueOf(System.currentTimeMillis() - startTimeJava) + " ms");

            scheduler.shutdown();
            try {
                scheduler.awaitTermination(500000L, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Java time taken : " + String.valueOf(System.currentTimeMillis() - startTimeJava) + " ms");

        }
        

        tasks = taskThroughput.createTasks(1_000_000);
        
        if (runEchoEcho) {
            long startTimeEchoEcho = System.currentTimeMillis();

            EchoEcho echoEcho = new EchoEcho(threads);

            for (Runnable task : tasks) {
                echoEcho.submit(task);
            }

            // System.out.println("EchoEcho submit time taken : " + String.valueOf(System.currentTimeMillis() - startTimeEchoEcho) + " ms");

            echoEcho.shutdown();
            echoEcho.awaitTermination(500000L);

            
            System.out.println("EchoEcho time taken : " + String.valueOf(System.currentTimeMillis() - startTimeEchoEcho) + " ms");
        }

        
    }
    
}
