package com.micorln.echoecho.demo;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.micorln.echoecho.core.EchoEcho;

public class MainPriority {
    public static void main(String[] args) {
        System.out.println("\n\n----");
        int numThreads = 1;
        EchoEcho echoEcho = new EchoEcho(numThreads);
        long startTime = System.currentTimeMillis();
        AtomicInteger threadsStarted = new AtomicInteger(0);
        int i = 0;
        Random random = new Random();
        for (i = 0; i < 5; i++) {
            
            long priority = random.nextInt(10);
            System.out.println("Submitting task : " + String.valueOf(i) + " with priority: " + priority);
            
            echoEcho.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread " + String.valueOf(threadsStarted.incrementAndGet()) + " says hello!");
            }, priority);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        echoEcho.shutdown();
        echoEcho.awaitTermination(20000L);
        System.out.println("Took  " + String.valueOf((System.currentTimeMillis() - startTime)) + " milli seconds to complete! ");

        
    }
}
