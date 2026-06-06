# EchoEcho - Custom Java Thread Pool Implementation

A production-inspired thread pool executor built from scratch to explore core Java concurrency concepts and patterns. EchoEcho implements task scheduling, worker management, priority-based execution, and graceful shutdown—all with underlying synchronization primitives.

## Overview

EchoEcho is an educational thread pool that mimics the behavior of Java's `ExecutorService` while exposing the internals of concurrent design. It's designed to teach:

- **Producer-Consumer patterns** with monitors and condition synchronization
- **Worker lifecycle management** and graceful shutdown coordination
- **Task metadata tracking** and execution telemetry
- **Priority-based scheduling** with custom comparators
- **Delayed task execution** with a dedicated scheduler thread
- **Exception isolation** to prevent worker thread death
- **Future-based async results** for both `Runnable` and `Callable` tasks

## Key Features

### 1. Core Thread Pool (`EchoEcho`)
- **Fixed-size worker thread pool** with on-demand creation up to the configured limit
- **Priority-based task queue** using a `PriorityQueue` for custom scheduling
- **Multiple task submission APIs**:
  - `submit(Runnable)` - Fire-and-forget tasks
  - `submit(Callable<T>)` - Tasks that return results
  - `submit(Runnable, priority)` - Tasks with explicit priority
  - `submit(Callable<T>, priority)` - Callable tasks with priority
- **Pool state management** (IDLE → RUNNING → SHUTTING_DOWN → STOPPED)
- **Graceful shutdown** with `awaitTermination(timeoutMillis)` to wait for worker completion

### 2. Worker Thread Management (`Worker`)
- **Worker lifecycle**: IDLE → RUNNING → INTERRUPTED/COMPLETED
- **Continuous task polling** from the shared queue with configurable timeout
- **Exception isolation**: Tasks that throw exceptions are caught and logged without crashing the worker
- **Idle timeout support**: Workers can exit after being idle for a specified duration
- **Cooperative interruption**: Workers respect thread interruption signals

### 3. Task Queue (`TaskQueue<T>`)
- **Thread-safe priority queue** backed by `PriorityQueue`
- **Monitor-based synchronization** using `wait()` and `notifyAll()`
- **Two polling modes**:
  - `pollTask()` - Blocking indefinitely until a task is available
  - `pollTask(timeToWait)` - Blocking with timeout for idle worker cleanup
- **Graceful shutdown** prevents new task submissions but allows pending tasks to complete
- **Generic design** supporting any `Comparable` task type

### 4. Task Wrapper Abstraction (`TaskWrapper<T>`)
- **Task metadata tracking**:
  - Unique task ID
  - Submission timestamp
  - Execution start/end times
  - Priority value
  - Associated `EchoFuture<T>` for async results
- **Subclasses**: `RunnableTask`, `CallableTask` for type-safe execution
- **Duration calculation** enables latency observability

### 5. Future-Based Results (`EchoFuture<T>`)
- **Non-blocking completion checking**: `hasTaskCompleted()`, `isTaskRunning()`
- **Blocking wait for results**:
  - `get()` - Wait indefinitely
  - `get(timeLimit)` - Wait with timeout
- **Volatile fields** for safe inter-thread visibility
- **Cancellation support**: `cancel()` prevents execution of cancelled tasks

### 6. Scheduled Task Execution (`ScheduledEchoEcho`)
- **Extends EchoEcho** with delayed task scheduling
- **Dedicated scheduler thread** that awakens tasks at their scheduled time
- **Recurring task support** via `scheduleWithDelay(Runnable, delayMs)`
- **Priority integration**: Uses negative timestamp as priority for scheduling order
- **Separate scheduled queue** with time-based ordering

### 7. Scheduled Future (`ScheduledEchoFuture<T>`)
- Wraps `EchoFuture<T>` for scheduled task results

## Architecture & Implementation Details

### Synchronization Strategy

EchoEcho uses **monitor-based synchronization** (traditional Java locks) rather than modern `java.util.concurrent` utilities:

```
┌──────────────────────────────────────────────┐
│         EchoEcho (Main Pool Manager)         │
│  synchronized methods for state transitions  │
└──────────────────────────────────────────────┘
                      ↓
    ┌────────────────────────────────────┐
    │  TaskQueue<TaskWrapper<?>>         │
    │  (Priority Queue + Monitors)       │
    │  - submit() wakes waiting workers  │
    │  - pollTask() waits for tasks      │
    └────────────────────────────────────┘
            ↓                    ↑
      (poll tasks)         (submit tasks)
            ↓                    ↑
┌─────────────────────────────────────────┐
│ Worker Threads (1..threadPoolSize)      │
│ - Continuous run() loop                 │
│ - Poll → Execute → Idle/Complete        │
│ - Exception-safe execution              │
└─────────────────────────────────────────┘
```

### State Transitions

**Pool States**:
```
IDLE → RUNNING → SHUTTING_DOWN → STOPPED
 ↑      ↓           ↓               
 └──────┴───────────┘ (loop while IDLE/RUNNING)
```

**Worker States**:
```
IDLE → RUNNING → IDLE (repeat)
            ↓
       INTERRUPTED / COMPLETED (final)
```

### Task Execution Flow

1. **Submit Phase**: Client calls `submit(task)` → Pool verifies state → Creates `TaskWrapper` → Adds to priority queue → Notifies waiting workers
2. **Scheduling Phase**: Worker calls `pollTask()` → Blocks if queue empty → Awakens when task available
3. **Execution Phase**: Worker extracts highest-priority task → Sets execution times → Invokes `task.run()` → Catches/logs exceptions
4. **Completion Phase**: Task completes or fails → `EchoFuture` marked as completed/failed → Client's `future.get()` unblocks

### Priority Scheduling

The `TaskQueue` uses a custom comparator for priority ordering:

```java
new TaskQueue<TaskWrapper<?>>((t1, t2) -> 
    Long.compare(t2.getPriority(), t1.getPriority())
)
```

Higher priority values execute first. In `ScheduledEchoEcho`, negative timestamps ensure older scheduled tasks run before newer ones.

### Graceful Shutdown Protocol

```
shutdown() called
    ↓
poolState = SHUTTING_DOWN
taskQueue.open = false (reject new submissions)
    ↓
Workers continue draining queue
    ↓
awaitTermination(timeoutMillis)
    ↓
Join with timeout on each worker
Force interrupt if deadline exceeded
    ↓
poolState = STOPPED
```

## Usage Examples

### Basic Submission

```java
EchoEcho pool = new EchoEcho(4); // 4 worker threads

// Submit a runnable task
EchoFuture<Void> future1 = pool.submit(() -> {
    System.out.println("Task 1 executing");
});

// Submit a callable task with result
EchoFuture<Integer> future2 = pool.submit(() -> {
    return 42;
});

Integer result = future2.get(); // Blocks until result available
System.out.println("Result: " + result);

pool.shutdown();
pool.awaitTermination(5000); // Wait up to 5 seconds for completion
```

### Priority-Based Execution

```java
EchoEcho pool = new EchoEcho(2);

pool.submit(() -> System.out.println("Low priority"), 1);
pool.submit(() -> System.out.println("High priority"), 10);
// "High priority" executes before "Low priority"

pool.shutdown();
pool.awaitTermination(1000);
```

### Scheduled Execution

```java
ScheduledEchoEcho scheduler = new ScheduledEchoEcho(4);

// Execute a task after 2 seconds
ScheduledEchoFuture<Void> future = scheduler.scheduleWithDelay(() -> {
    System.out.println("Delayed task executed");
}, 2000);

scheduler.shutdown();
scheduler.awaitTermination(5000);
```

### Exception Handling

```java
EchoEcho pool = new EchoEcho(2);

// Task that throws an exception
EchoFuture<Void> future = pool.submit(() -> {
    throw new RuntimeException("Task failed!");
});

// Worker logs the exception and continues
// Other workers and tasks remain unaffected

pool.shutdown();
pool.awaitTermination(1000);
```

## Technical Deep Dives

### Monitor-Based Wait/Notify

The `TaskQueue` uses Java's low-level synchronization primitives:

```java
// Producer side (submit)
synchronized void submit(T task) {
    taskQueue.add(task);
    notifyAll(); // Wake all waiting workers
}

// Consumer side (poll with timeout)
synchronized T pollTask(long timeToWait) throws InterruptedException {
    if (taskQueue.size() == 0) {
        wait(timeToWait); // Sleep until timeout or notification
    }
    return taskQueue.poll();
}
```

**Why this matters**: Teaches condition coordination without modern constructs like `Condition`, `CountDownLatch`, or `Semaphore`.

### Volatile Visibility

Worker state and pool state use `volatile` for safe inter-thread visibility:

```java
private volatile WorkerState workerState;
private volatile PoolState poolState;
```

Ensures changes are immediately visible across threads without additional synchronization.

### Future Implementation (Busy-Wait)

`EchoFuture.get()` uses a busy-wait loop:

```java
public T get() {
    while (!hasCompleted && !hasFailed) {
        // Spin until completion
    }
    return resultValue;
}
```

**Note**: This is educational; production code would use `CountDownLatch` or `Condition.await()`.

## Concurrency Concepts Taught

| Concept | Where | How |
|---------|-------|-----|
| **Producer-Consumer** | TaskQueue | Threads submit and poll with synchronization |
| **Monitors** | TaskQueue, EchoEcho | Synchronized blocks with wait/notifyAll |
| **Race Conditions** | Pool/Worker state | AtomicInteger for task ID generation |
| **Volatile Visibility** | PoolState, WorkerState | Cross-thread state observation |
| **Graceful Shutdown** | shutdown(), awaitTermination() | Coordinated lifecycle |
| **Exception Isolation** | Worker.run() | try/catch prevents thread death |
| **Priority Scheduling** | TaskQueue with Comparator | Higher priority executes first |
| **Async Results** | EchoFuture | Futures for non-blocking completion |
| **Lifecycle Management** | PoolState, WorkerState enums | State machines for coordination |
| **Delayed Execution** | ScheduledEchoEcho | Separate scheduler thread with time tracking |

## Project Structure

```
echo-echo/
├── pom.xml                               # Maven configuration (Java 21)
├── src/
│   ├── main/java/com/micorln/echoecho/
│   │   ├── core/
│   │   │   ├── EchoEcho.java            # Main pool manager
│   │   │   ├── ScheduledEchoEcho.java   # Scheduled variant
│   │   │   ├── Worker.java              # Worker thread implementation
│   │   │   ├── TaskQueue.java           # Thread-safe priority queue
│   │   │   ├── TaskWrapper.java         # Abstract task base
│   │   │   ├── RunnableTask.java        # Runnable wrapper
│   │   │   ├── CallableTask.java        # Callable wrapper
│   │   │   ├── EchoFuture.java          # Future for results
│   │   │   ├── ScheduledTask.java       # Scheduled task wrapper
│   │   │   ├── ScheduledEchoFuture.java # Scheduled future
│   │   │   ├── PoolState.java           # Enum: IDLE, RUNNING, etc.
│   │   │   └── WorkerState.java         # Enum: IDLE, RUNNING, etc.
│   │   └── demo/
│   │       ├── Main.java                # Basic usage example
│   │       ├── MainFuture.java          # Callable/Future example
│   │       ├── MainPriority.java        # Priority scheduling example
│   │       ├── MainException.java       # Exception handling example
│   │       ├── ScheduledMain.java       # Scheduled tasks example
│   │       └── MainWorkerRecycle.java   # Worker lifecycle example
│   └── test/
│       └── java/.../AppTest.java        # Unit tests
```

## Compilation & Execution

### Build

```bash
mvn clean compile
```

### Run Examples

```bash
mvn exec:java -Dexec.mainClass="com.micorln.echoecho.demo.Main"
mvn exec:java -Dexec.mainClass="com.micorln.echoecho.demo.MainFuture"
mvn exec:java -Dexec.mainClass="com.micorln.echoecho.demo.MainPriority"
```

### Run Tests

```bash
mvn test
```

## Limitations & Learning Notes

1. **Busy-Wait in EchoFuture**: The `get()` method spins rather than blocking efficiently. In production, use `Condition.await()`.

2. **No Task Cancellation During Execution**: Once a task starts running, it cannot be interrupted cleanly. A production pool would support interruption-aware tasks.

3. **Synchronization Coarseness**: Methods are entirely synchronized, limiting concurrency. Production pools use fine-grained locking or lock-free data structures.

4. **No Metrics/Observability**: No built-in counters for throughput, latency, or rejection. Extensions could add Prometheus metrics.

5. **Fixed Size Only**: The current design creates workers on-demand up to the limit but doesn't dynamically scale. Extension: add min/max sizing with idle timeouts.

6. **No Task Timeout**: Long-running tasks can block the pool indefinitely. Extension: add per-task timeouts with cancellation.

## Design Patterns Used

- **Template Method** (TaskWrapper hierarchy - abstract run behavior)
- **Strategy** (Comparator for priority ordering)
- **Monitor** (TaskQueue synchronization)
- **Lazy Initialization** (Workers created on-demand)
- **State Machine** (PoolState, WorkerState)
- **Wrapper/Decorator** (TaskWrapper around Runnable/Callable)

## Further Reading

- **Java Concurrency in Practice** (Goetz et al.) - Chapters 6-8 on thread pools and executors
- **The Java Language Specification** - Chapter 17 on memory model and visibility
- **Java Virtual Machine Specification** - Object locking and synchronization details

## License

Educational project. Free to use and modify.

## Author Notes

This implementation prioritizes **clarity and educational value** over production-grade performance. It's designed to expose concurrency mechanics that are often hidden by `java.util.concurrent` abstractions. Study the synchronization patterns, state machines, and lifecycle coordination to understand how real executors work under the hood.
