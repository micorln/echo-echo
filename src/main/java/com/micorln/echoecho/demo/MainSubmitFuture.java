package com.micorln.echoecho.demo;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.micorln.echoecho.core.EchoEcho;
import com.micorln.echoecho.core.EchoFuture;

public class MainSubmitFuture {

    public static void main(String[] args) {
        System.out.println("\n\n----");
        int numThreads = 1;
        int numScheduledDemoThreads = 5;
        EchoEcho echoEcho = new EchoEcho(numThreads);
        long startTime = System.currentTimeMillis();
        AtomicInteger threadsStarted = new AtomicInteger(0);
        int i = 0;
        ArrayList<EchoFuture<Void>> futures = new ArrayList<>();
       
        for (i = 0; i < numScheduledDemoThreads; i++) {
            System.out.println("Submitting task : " + String.valueOf(i+1));
            EchoFuture<Void> future = echoEcho.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread " + String.valueOf(threadsStarted.incrementAndGet()) + " says hello!");
            });
            futures.add(future);
        }

        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        for (EchoFuture<Void> future : futures) {
            future.cancel();
        }

        echoEcho.shutdown();
        echoEcho.awaitTermination(5000L);
        System.out.println("Took  " + String.valueOf((System.currentTimeMillis() - startTime)) + " milli seconds to complete! ");
    }
    
}
