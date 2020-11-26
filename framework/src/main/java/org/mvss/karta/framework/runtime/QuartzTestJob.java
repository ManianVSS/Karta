package org.mvss.karta.framework.runtime;

import java.util.concurrent.atomic.AtomicLong;

import org.mvss.karta.framework.core.TestJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
public class QuartzTestJob implements Job
{

   @Override
   public void execute( JobExecutionContext context ) throws JobExecutionException
   {
      KartaRuntime kartaRuntime = null;

      try
      {
         JobDataMap jobData = context.getJobDetail().getJobDataMap();
         kartaRuntime = (KartaRuntime) jobData.get( Constants.KARTA_RUNTIME );
         RunInfo runInfo = (RunInfo) jobData.get( Constants.RUN_INFO );
         String featureName = (String) jobData.get( Constants.FEATURE_NAME );
         TestJob testJob = (TestJob) jobData.get( Constants.TEST_JOB );
         AtomicLong iterationCounter = (AtomicLong) jobData.get( Constants.ITERATION_COUNTER );

         TestJobRunner.run( kartaRuntime, runInfo, featureName, testJob, iterationCounter.getAndIncrement() );
      }
      catch ( Throwable e )
      {
         log.error( e );
      }
   }

}
