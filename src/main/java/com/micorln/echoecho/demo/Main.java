package com.micorln.echoecho.demo;

import java.util.concurrent.atomic.AtomicInteger;

import com.micorln.echoecho.core.EchoEcho;

public class Main {

    public static void main(String[] args) {
        System.out.println("\n\n----");
        int numThreads = 10;
        EchoEcho echoEcho = new EchoEcho(numThreads);
        long startTime = System.currentTimeMillis();
        AtomicInteger threadsStarted = new AtomicInteger(0);
        int i = 0;
        for (i = 0; i < numThreads; i++) {
            System.out.println("Submitting task : " + String.valueOf(i));
            echoEcho.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread " + String.valueOf(threadsStarted.incrementAndGet()) + " says hello!");
            });
        }
        echoEcho.shutdown();
        echoEcho.awaitTermination(2000L);
        System.out.println("Took  " + String.valueOf((System.currentTimeMillis() - startTime)) + " milli seconds to complete! ");

    }
    
}
