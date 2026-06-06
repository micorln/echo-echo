package com.micorln.echoecho.core;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScheduledEchoEcho extends EchoEcho {

    TaskQueue<ScheduledTask<?>> scheduledTaskQueue;

    private AtomicBoolean closed;

    private final long START_TIME = System.currentTimeMillis();

    public ScheduledEchoEcho(int threadPoolSize) {
        super(threadPoolSize);
        this.scheduledTaskQueue = new TaskQueue<ScheduledTask<?>>((t1, t2) -> Long.compare(t1.getNextExecutionTime(), t2.getNextExecutionTime()));
        closed = new AtomicBoolean(false);
        Thread scheduler = new Thread(this::run);
        scheduler.start();
        
    }

    public synchronized ScheduledEchoFuture<Void> scheduleWithDelay(Runnable task, long durationInMilliseconds) {
        ifClosed();
        System.out.println("Received request to schedule a runnable task to run after " + String.valueOf(durationInMilliseconds) + " milliseconds.");
        ScheduledEchoFuture<Void> scheduledEchoFuture = new ScheduledEchoFuture<Void>();
        RunnableTask runnableTask = new RunnableTask(task, super.taskIds.incrementAndGet());
        ScheduledTask<Void> scheduledTask = new ScheduledTask<>(runnableTask, System.currentTimeMillis() + durationInMilliseconds, durationInMilliseconds);
        
        scheduledTaskQueue.submit(scheduledTask);
        System.out.println("Next execution time for task with id : " + String.valueOf(runnableTask.getTaskId()) + " is " + String.valueOf(scheduledTask.getNextExecutionTime() - START_TIME));
        scheduledEchoFuture.setEchoFuture(runnableTask.getFuture());
        System.out.println("Scheduled task with id : " + String.valueOf(runnableTask.getTaskId()) + " to run after " + String.valueOf(durationInMilliseconds) + " milliseconds.");
        notifyAll();
        return scheduledEchoFuture;       
    }

    private synchronized void run() {
        while (!closed.get()) {
            while (scheduledTaskQueue.size() == 0) {
                try {
                    System.out.println("Going into wait state as there are no scheduled tasks to execute.");
                    wait();
                    System.out.println("Woke up from wait state.");
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            ScheduledTask<?> topTask = scheduledTaskQueue.peek();
            while (topTask == null || topTask.getNextExecutionTime() > System.currentTimeMillis()) {
                try {
                    wait(topTask.getNextExecutionTime() - System.currentTimeMillis());
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (closed.get()) {
                System.out.println("Scheduled thread is closed, no more tasks will be executed.");
                return;
            }
            try {
                ScheduledTask<?> task = scheduledTaskQueue.pollTask();
                if (task != null) {
                    if (task.getTask().getFuture().hasTaskFailed()) {
                        System.out.println("Task with id : " + String.valueOf(task.getTask().getTaskId()) + " has been cancelled. Not executing task.");
                        continue;
                    }
                    System.out.println("Executing task with id : " + String.valueOf(task.getTask().getTaskId()));
                    submit(task.getTask(), -1*task.getNextExecutionTime());
                    task.setNextExecutionTime(System.currentTimeMillis() + task.getDelay());
                    System.out.println("Next execution time for task with id : " + String.valueOf(task.getTask().getTaskId()) + " is " + String.valueOf(task.getNextExecutionTime() - START_TIME));
                    scheduledTaskQueue.submit(task);
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public synchronized void shutdown() {
        closed.set(true);
        super.shutdown();
    }
    
    private synchronized void ifClosed() {
        if (closed.get()) {
            throw new IllegalStateException("ScheduledEchoEcho is closed, cannot schedule new tasks.");
        }
    }
}
