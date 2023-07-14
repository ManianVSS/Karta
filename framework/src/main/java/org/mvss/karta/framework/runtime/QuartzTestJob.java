package org.mvss.karta.framework.runtime;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.BeanRegistry;
import org.mvss.karta.dependencyinjection.TestProperties;
import org.mvss.karta.framework.interfaces.TestJobIterationResultProcessor;
import org.mvss.karta.framework.models.result.TestJobResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.TestJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@NoArgsConstructor
public class QuartzTestJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        try {
            JobDataMap jobData = context.getJobDetail().getJobDataMap();
            KartaRuntime kartaRuntime = (KartaRuntime) jobData.get(Constants.KARTA_RUNTIME);
            RunInfo runInfo = (RunInfo) jobData.get(Constants.RUN_INFO);
            String featureName = (String) jobData.get(Constants.FEATURE_NAME);
            TestProperties testProperties = (TestProperties) jobData.get(Constants.TEST_PROPERTIES);
            TestJob testJob = (TestJob) jobData.get(Constants.TEST_JOB);
            AtomicInteger iterationCounter = (AtomicInteger) jobData.get(Constants.ITERATION_COUNTER);
            TestJobIterationResultProcessor testJobIterationResultProcessor = (TestJobIterationResultProcessor) jobData.get(Constants.TEST_JOB_ITERATION_RESULT_PROCESSOR);
            BeanRegistry contextBeanRegistry = (BeanRegistry) jobData.get(Constants.BEAN_REGISTRY);

            // Run the job iteration on a remote node or local node using utility method
            TestJobResult testJobResult = kartaRuntime.runJobIteration(runInfo, featureName, testProperties, testJob, iterationCounter.getAndIncrement(), contextBeanRegistry);

            if (testJobIterationResultProcessor != null) {
                testJobIterationResultProcessor.consume(testJob.getName(), testJobResult);
            }
        } catch (Throwable e) {
            log.error(e);
        }
    }
}
