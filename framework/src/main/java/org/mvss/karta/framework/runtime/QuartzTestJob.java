package org.mvss.karta.framework.runtime;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
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
      String runName = Constants.UNNAMED;

      try
      {
         JobDataMap jobData = context.getJobDetail().getJobDataMap();
         kartaRuntime = (KartaRuntime) jobData.get( "kartaRuntime" );
         StepRunner stepRunner = (StepRunner) jobData.get( "stepRunner" );
         @SuppressWarnings( "unchecked" )
         ArrayList<TestDataSource> testDataSources = (ArrayList<TestDataSource>) jobData.get( "testDataSources" );
         runName = (String) jobData.get( "runName" );
         TestFeature feature = (TestFeature) jobData.get( "testFeature" );
         TestJob testJob = (TestJob) jobData.get( "testJob" );
         AtomicInteger iterationCounter = (AtomicInteger) jobData.get( "iterationCounter" );

         TestJobRunner.run( kartaRuntime, stepRunner, testDataSources, runName, feature, testJob, iterationCounter.getAndIncrement() );
      }
      catch ( Throwable e )
      {
         log.error( e );
      }
   }

}
