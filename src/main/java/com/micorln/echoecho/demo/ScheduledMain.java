package com.micorln.echoecho.demo;

import java.util.concurrent.atomic.AtomicInteger;

import com.micorln.echoecho.core.ScheduledEchoEcho;
import com.micorln.echoecho.core.ScheduledEchoFuture;

public class ScheduledMain {

    public static void main(String[] args) {
        
        ScheduledEchoEcho scheduledEchoEcho = new ScheduledEchoEcho(1);
        System.out.println("Starting Schedule Main to test schedule with delay!");
        long startTime = System.currentTimeMillis();
        AtomicInteger threadsStarted = new AtomicInteger(0);
        
        ScheduledEchoFuture<Void> scheduledFuture = scheduledEchoEcho.scheduleWithDelay(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread " + String.valueOf(threadsStarted.incrementAndGet()) + " says hello!");
            }, 1000L);

        ScheduledEchoFuture<Void> scheduledFuture1 = scheduledEchoEcho.scheduleWithDelay(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread " + String.valueOf(threadsStarted.incrementAndGet()) + " says good day!");
            }, 900L);

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        scheduledFuture.cancel();
        scheduledFuture1.cancel();
        scheduledEchoEcho.shutdown();
        System.out.println("Time taken to complete : " + String.valueOf((System.currentTimeMillis() - startTime)) + " milli seconds! ");
    }
    
}
