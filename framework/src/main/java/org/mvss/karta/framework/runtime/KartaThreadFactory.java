package org.mvss.karta.framework.runtime;

import org.mvss.karta.Constants;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class KartaThreadFactory implements ThreadFactory {
    private final AtomicInteger threadCounter = new AtomicInteger();

    private final ConcurrentHashMap<KartaRunnable, Thread> kartaRunnableThreadMap = new ConcurrentHashMap<>();

    private final Object lock = new Object();

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, Constants.KARTA + Constants.HYPHEN + threadCounter.incrementAndGet());

        synchronized (lock) {
            if (r instanceof KartaRunnable) {
                kartaRunnableThreadMap.put((KartaRunnable) r, thread);
            }
        }
        clearNonRunningThreads();

        return thread;
    }

    public void clearNonRunningThreads() {
        for (KartaRunnable kartaRunnable : kartaRunnableThreadMap.keySet()) {
            if (!kartaRunnableThreadMap.get(kartaRunnable).isAlive()) {
                kartaRunnableThreadMap.remove(kartaRunnable);
            }
        }
    }

    public void shutdown() {
        synchronized (lock) {
            for (KartaRunnable kartaRunnable : kartaRunnableThreadMap.keySet()) {
                if (kartaRunnableThreadMap.get(kartaRunnable).isAlive()) {
                    kartaRunnable.shutdown();
                }
            }
            kartaRunnableThreadMap.clear();
        }
    }

}
