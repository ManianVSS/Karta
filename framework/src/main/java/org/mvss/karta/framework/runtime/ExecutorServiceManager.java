package org.mvss.karta.framework.runtime;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class to store and manage ExecutorService for various thread groups.
 *
 * @author Manian
 */
@NoArgsConstructor
@Log4j2
public class ExecutorServiceManager implements AutoCloseable {
    /**
     * The thread group name to ExecutorService mapping
     */
    private final HashMap<String, ExecutorService> executorServicesMap = new HashMap<>();

    /**
     * Synchronization object
     */
    private final Object executorSyncObject = new Object();

    /**
     * Get the ExecutorService for the thread group by group name.
     */
    public ExecutorService getExecutorServiceForGroup(String group) {
        return getOrAddExecutorServiceForGroup(group, 1);
    }

    /**
     * Get the ExecutorService for the thread group by name or add a new one with the thread count
     */
    public ExecutorService getOrAddExecutorServiceForGroup(String group, int threadCount) {
        if (StringUtils.isEmpty(group)) {
            group = Constants.__DEFAULT__;
        }

        synchronized (executorSyncObject) {
            if (!executorServicesMap.containsKey(group)) {
                executorServicesMap.put(group,
                        new ThreadPoolExecutor(threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue(threadCount)));
            }
        }

        return executorServicesMap.get(group);
    }

    /**
     * Add thread groups from the HashMap with the mapped thread counts.
     */
    public void addExecutorServiceForGroups(HashMap<String, Integer> threadGroupMap) {
        for (Entry<String, Integer> entry : threadGroupMap.entrySet()) {
            getOrAddExecutorServiceForGroup(entry.getKey(), entry.getValue());
        }
    }

    /**
     * AutoCloseable implementation to shut down all executors before closing
     */
    @Override
    public void close() {
        synchronized (executorSyncObject) {
            // Trigger shutdown for all executor services first.
            for (ExecutorService executorService : executorServicesMap.values()) {
                if (!executorService.isShutdown()) {
                    executorService.shutdown();
                }
            }

            // Wait for all thread groups to shut down.
            for (ExecutorService executorService : executorServicesMap.values()) {
                try {
                    if (!executorService.isShutdown()) {
                        executorService.shutdown();
                    }

                    if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                        log.error("");
                    }
                } catch (InterruptedException e) {
                    log.error("Was interrupted during a shut down");
                }
            }
        }
    }
}
