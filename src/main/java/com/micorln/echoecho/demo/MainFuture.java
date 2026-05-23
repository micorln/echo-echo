package com.micorln.echoecho.demo;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.micorln.echoecho.core.EchoEcho;
import com.micorln.echoecho.core.EchoFuture;

public class MainFuture {

    public static void main(String[] args) {
        System.out.println("\n\n----");
        int numThreads = 10;
        EchoEcho echoEcho = new EchoEcho(numThreads);
        long startTime = System.currentTimeMillis();
        AtomicInteger threadsStarted = new AtomicInteger(0);
        int i = 0;
        ArrayList<EchoFuture<String>> futures = new ArrayList<>();
        for (i = 0; i < numThreads; i++) {
            System.out.println("Submitting task : " + String.valueOf(i));
            EchoFuture <String> future = echoEcho.submit(() -> {
                Thread.sleep(1000);
                System.out.println("Thread " + String.valueOf(threadsStarted.incrementAndGet()) + " says hello!");
                return "Hello from thread " + String.valueOf(threadsStarted.incrementAndGet());
            });
            futures.add(future);
        }
        for (EchoFuture<String> f : futures) {
            String res = f.get();
            System.out.println("Got result from thread : " + res);
        }

        echoEcho.shutdown();
        echoEcho.awaitTermination(2000L);
        System.out.println("Took  " + String.valueOf((System.currentTimeMillis() - startTime)) + " milli seconds to complete! ");

    }
    
}
