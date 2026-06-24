package com.micorln.echoecho.benchmarking;

import java.util.concurrent.*;

public class LatencyBenchmark extends Benchmarker {
    
    public LatencyBenchmark(int numThreads) {
        super(numThreads);
    }
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Submit Latency Benchmark (10,000 tasks) ===\n");
        
        LatencyBenchmark bench = new LatencyBenchmark(4);
        
        // Warmup
        System.out.println("Warming up...");
        for (int i = 0; i < 100; i++) {
            bench.submit(() -> {});
        }
        Thread.sleep(500);
        
        // Measure EchoEcho latency
        System.out.println("\n=== EchoEcho Latency ===");
        long[] echoLatencies = new long[10000];
        for (int i = 0; i < 10000; i++) {
            long start = System.nanoTime();
            bench.submit(() -> {
                int x = 1;
                x++;
            });
            long end = System.nanoTime();
            echoLatencies[i] = (end - start) / 1000; // Convert to microseconds
        }
        
        bench.threadPool.shutdown();
        bench.threadPool.awaitTermination(500000L);
        
        // Measure Java latency
        System.out.println("\n=== Java ScheduledExecutor Latency ===");
        long[] javaLatencies = new long[10000];
        for (int i = 0; i < 10000; i++) {
            long start = System.nanoTime();
            bench.javaSubmit(() -> {
                int x = 1;
                x++;
            });
            long end = System.nanoTime();
            javaLatencies[i] = (end - start) / 1000; // Convert to microseconds
        }
        
        bench.scheduler.shutdown();
        try {
            bench.scheduler.awaitTermination(500000L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Print stats
        printStats("EchoEcho", echoLatencies);
        printStats("Java ScheduledExecutor", javaLatencies);
    }
    
    static void printStats(String name, long[] latencies) {
        java.util.Arrays.sort(latencies);
        
        double sum = 0;
        for (long lat : latencies) {
            sum += lat;
        }
        double avg = sum / latencies.length;
        
        long p50 = latencies[latencies.length / 2];
        long p95 = latencies[(int)(latencies.length * 0.95)];
        long p99 = latencies[(int)(latencies.length * 0.99)];
        long max = latencies[latencies.length - 1];
        
        System.out.println(name + " submit() latency (microseconds):");
        System.out.println("  Average: " + String.format("%.2f", avg));
        System.out.println("  P50:     " + p50);
        System.out.println("  P95:     " + p95);
        System.out.println("  P99:     " + p99);
        System.out.println("  Max:     " + max);
    }
}
