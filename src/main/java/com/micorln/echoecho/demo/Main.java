package com.micorln.echoecho.demo;

import java.util.concurrent.atomic.AtomicInteger;

import com.micorln.echoecho.core.EchoEcho;

public class Main {

    public static void main(String[] args) {
        System.out.println("\n\n----");
        int numThreads = 3;
        EchoEcho echoEcho = new EchoEcho(numThreads);
        long startTime = System.currentTimeMillis();
        AtomicInteger threadsStarted = new AtomicInteger(0);
        for (int i = 0; i < numThreads; i++) {
            System.out.println("Submitting task : " + String.valueOf(i));
            echoEcho.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread " + String.valueOf(threadsStarted.get()) + " says hello!");
                threadsStarted.incrementAndGet();
            });
        }
        echoEcho.shutdown();
        System.out.println("Took  " + String.valueOf((System.currentTimeMillis() - startTime)) + " seconds to complete! ");

    }
    
}
