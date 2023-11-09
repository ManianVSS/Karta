package org.mvss.karta.framework.threading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class KartaThreadFactory implements ThreadFactory {

    public static final String KARTA_RUNNER_THREAD = "karta-runner-thread-";

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public KartaThreadFactory() {
        this.namePrefix = KARTA_RUNNER_THREAD + poolNumber.getAndIncrement();
    }

    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, this.namePrefix + this.threadNumber.getAndIncrement());
    }
}
