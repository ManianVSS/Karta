package org.mvss.karta.framework.runtime;

import lombok.extern.log4j.Log4j2;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@DisallowConcurrentExecution
public class QuartzJobScheduler
{
   private static final SchedulerFactory schedulerFactory = new StdSchedulerFactory();
   private static       Scheduler        scheduler        = null;
   private static final AtomicLong       jobCounter       = new AtomicLong();

   private static final String JOB_NAME_PREFIX     = "KartaQuartzJob";
   private static final String JOB_GROUP           = Constants.__KARTA__;
   private static final String TRIGGER_NAME_PREFIX = "KartaQuartzJobTrigger_";
   private static final String TRIGGER_GROUP       = Constants.__KARTA__;

   static
   {
      try
      {
         init();
      }
      catch ( SchedulerException se )
      {
         log.error( se );
         System.exit( -1 );
      }
   }

   public static void init() throws SchedulerException
   {
      scheduler = schedulerFactory.getScheduler();
      scheduler.start();
   }

   public static long scheduleJob( Class<? extends Job> jobClass, long scheduleInterval, int repeatCount, HashMap<String, Object> jobParams )
            throws SchedulerException
   {
      JobBuilder jobBuilder = JobBuilder.newJob( jobClass );
      JobDataMap jobDataMap = new JobDataMap();
      jobDataMap.putAll( jobParams );

      long      jobCount  = jobCounter.getAndIncrement();
      JobDetail jobDetail = jobBuilder.setJobData( jobDataMap ).withIdentity( JOB_NAME_PREFIX + jobCount, JOB_GROUP ).build();
      Trigger trigger = TriggerBuilder.newTrigger().withIdentity( TRIGGER_NAME_PREFIX + jobCount, TRIGGER_GROUP ).startNow().withSchedule(
               SimpleScheduleBuilder.simpleSchedule().withRepeatCount( repeatCount ).withMisfireHandlingInstructionIgnoreMisfires()
                        .withIntervalInMilliseconds( scheduleInterval ) ).build();
      scheduler.scheduleJob( jobDetail, trigger );
      return jobCount;
   }

   public static boolean deleteJob( long jobId )
   {
      try
      {
         return scheduler.deleteJob( JobKey.jobKey( JOB_NAME_PREFIX + jobId, JOB_GROUP ) );
      }
      catch ( SchedulerException e )
      {
         log.error( e );
         return false;
      }
   }

   public static boolean deleteJobs( List<Long> jobIds )
   {
      boolean jobDeletionStatus = true;

      for ( Long jobId : jobIds )
      {
         if ( jobId >= 0 )
         {
            jobDeletionStatus = jobDeletionStatus && deleteJob( jobId );
         }
      }

      return jobDeletionStatus;
   }

   public static void shutdown() throws SchedulerException
   {
      scheduler.clear();
      scheduler.shutdown();
   }
}
