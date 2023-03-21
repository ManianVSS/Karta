package org.mvss.karta.framework.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.BeanRegistry;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.TestJob;

@Log4j2
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DaemonTestJob implements Runnable {
    private KartaRuntime kartaRuntime;
    private RunInfo runInfo;
    private String featureName;
    private TestJob testJob;
    private BeanRegistry contextBeanRegistry;

    @Override
    public void run() {
        try {
            log.info("Starting daemon test job " + testJob.getName());
            // Run the job iteration on a remote node or local node using utility method
            kartaRuntime.runJobIteration(runInfo, featureName, testJob, -1, contextBeanRegistry);
        } catch (InterruptedException e) {
            log.info("Stopping daemon test job " + testJob.getName());
        } catch (Throwable e) {
            log.error("Exception in daemon test job " + testJob.getName(), e);
        }
    }
}
