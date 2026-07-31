package com.colony.mod.performance;

import com.colony.mod.ColonyMod;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Single-threaded background executor for expensive Colony AI calculations.
 *
 * <p>Heavy operations — GOAP backward-chaining and Utility AI scoring — are offloaded here so the
 * main server tick thread stays at 20 TPS. The results are cached on the colonist entity and
 * consumed on the next main-thread tick.
 *
 * <p>There is exactly one daemon thread. If the queue fills up, new submissions are silently
 * discarded (the colonist will retry next evaluation cycle).
 *
 * <p>Must be shut down via {@link #shutdown()} when the server stops to avoid thread leaks.
 */
public final class ColonyAIExecutor {

    private static final int MAX_QUEUE_DEPTH = 256;

    private static ColonyAIExecutor INSTANCE;

    private final ThreadPoolExecutor executor;

    private ColonyAIExecutor() {
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "colony-ai-" + count.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 1);
                return t;
            }
        };

        // Single worker thread; bounded queue; discard oldest on overflow to avoid lag spikes
        this.executor = new ThreadPoolExecutor(
                1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(MAX_QUEUE_DEPTH),
                factory,
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    // -------------------------------------------------------------------------
    // Singleton lifecycle
    // -------------------------------------------------------------------------

    /** Returns (or lazily creates) the shared executor instance. */
    public static ColonyAIExecutor getInstance() {
        if (INSTANCE == null || INSTANCE.executor.isShutdown()) {
            INSTANCE = new ColonyAIExecutor();
            ColonyMod.LOGGER.info("[Colony] ColonyAIExecutor started.");
        }
        return INSTANCE;
    }

    /**
     * Shuts down the executor gracefully. Call this from {@code ServerStoppingEvent}.
     * Waits up to 2 seconds for in-flight tasks to complete.
     */
    public static void shutdown() {
        if (INSTANCE != null && !INSTANCE.executor.isShutdown()) {
            INSTANCE.executor.shutdown();
            try {
                if (!INSTANCE.executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    INSTANCE.executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                INSTANCE.executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            ColonyMod.LOGGER.info("[Colony] ColonyAIExecutor shut down.");
            INSTANCE = null;
        }
    }

    // -------------------------------------------------------------------------
    // Task submission
    // -------------------------------------------------------------------------

    /**
     * Submits a callable AI computation task and returns a {@link Future} for the result.
     *
     * <p>If the queue is full the oldest task is evicted (DiscardOldestPolicy). The caller
     * should check {@code future.isDone()} before consuming the result.
     *
     * @param task the AI computation (must be thread-safe; must not mutate world state)
     * @param <T>  result type
     * @return a future for the result
     */
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * Returns the number of tasks currently waiting in the queue.
     */
    public int getQueueDepth() {
        return executor.getQueue().size();
    }
}
