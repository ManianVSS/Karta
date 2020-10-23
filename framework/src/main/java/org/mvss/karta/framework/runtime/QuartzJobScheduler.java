package org.mvss.karta.framework.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import lombok.extern.log4j.Log4j2;

@Log4j2
@DisallowConcurrentExecution
public class QuartzJobScheduler
{
   private static SchedulerFactory schedulerFactory    = new StdSchedulerFactory();
   private static Scheduler        scheduler           = null;
   private static AtomicInteger    jobCounter          = new AtomicInteger();

   private static final String     JOB_NAME_PREFIX     = "KartaQuartzJob";
   private static final String     JOB_GROUP           = "__Karta__";
   private static final String     TRIGGER_NAME_PREFIX = "KartaQuartzJobTrigger_";
   private static final String     TRIGGER_GROUP       = "__Karta__";

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

   public static int scheduleJob( Class<? extends Job> jobClass, long scheduleInterval, HashMap<String, Object> jobParams ) throws SchedulerException
   {
      JobBuilder jobBuilder = JobBuilder.newJob( jobClass );
      JobDataMap jobDataMap = new JobDataMap();
      jobDataMap.putAll( jobParams );

      int jobCount = jobCounter.getAndIncrement();
      JobDetail jobDetail = jobBuilder.setJobData( jobDataMap ).withIdentity( JOB_NAME_PREFIX + jobCount, JOB_GROUP ).build();
      Trigger trigger = TriggerBuilder.newTrigger().withIdentity( TRIGGER_NAME_PREFIX + jobCount, TRIGGER_GROUP ).startNow()
               .withSchedule( SimpleScheduleBuilder.simpleSchedule().withRepeatCount( SimpleTrigger.REPEAT_INDEFINITELY ).withMisfireHandlingInstructionIgnoreMisfires().withIntervalInMilliseconds( scheduleInterval ) ).build();
      scheduler.scheduleJob( jobDetail, trigger );
      return jobCount;
   }

   public static boolean deleteJob( int jobId )
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

   public static boolean deleteJobs( List<Integer> jobIds )
   {
      boolean jobDeletionStatus = true;

      for ( Integer jobId : jobIds )
      {
         jobDeletionStatus = jobDeletionStatus && deleteJob( jobId );
      }

      return jobDeletionStatus;
   }

   public static void shutdown() throws SchedulerException
   {
      scheduler.clear();
      scheduler.shutdown();
   }
}
