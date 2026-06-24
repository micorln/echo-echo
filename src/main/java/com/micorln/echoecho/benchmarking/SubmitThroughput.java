package com.micorln.echoecho.benchmarking;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.micorln.echoecho.core.EchoEcho;

public class SubmitThroughput extends Benchmarker {
    
    ArrayList<Runnable> tasks;

    public SubmitThroughput(int numThreads) {
        super(numThreads);
        tasks = new ArrayList<>();
    }

    public ArrayList<Runnable> createTasks(int numTasks) {
        tasks = new ArrayList<>();
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
        return tasks;
    }

    public static void main(String[] args) {

        int threads = 4;
        SubmitThroughput submitThroughput = new SubmitThroughput(4);
        ArrayList<Runnable> tasks = submitThroughput.createTasks(100000);

        boolean runJava = false;
        boolean runEchoEcho = true;

        if (runJava) {

            long startTimeJava = System.currentTimeMillis();
            ExecutorService scheduler = Executors.newFixedThreadPool(threads);
            for (Runnable task : tasks) {
                scheduler.submit(task);
            }

            System.out.println("Java submit time taken : " + String.valueOf(System.currentTimeMillis() - startTimeJava) + " ms");

            scheduler.shutdown();
            try {
                scheduler.awaitTermination(500000L, java.util.concurrent.TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            

        }
        

        tasks = submitThroughput.createTasks(1_000_000);
        
        if (runEchoEcho) {
            long startTimeEchoEcho = System.currentTimeMillis();

            EchoEcho echoEcho = new EchoEcho(threads);

            for (Runnable task : tasks) {
                echoEcho.submit(task);
            }

            System.out.println("EchoEcho submit time taken : " + String.valueOf(System.currentTimeMillis() - startTimeEchoEcho) + " ms");

            echoEcho.shutdown();
            echoEcho.awaitTermination(500000L);

            
            
        }


        

        
    }

}
