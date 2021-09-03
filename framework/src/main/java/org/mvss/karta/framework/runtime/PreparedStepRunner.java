package org.mvss.karta.framework.runtime;

import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;
import lombok.*;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@Builder
public class PreparedStepRunner implements Callable<StepResult>
{
   private KartaRuntime         kartaRuntime;
   private RunInfo              runInfo;
   private String               nodeName;
   private PreparedStep         step;
   private Consumer<StepResult> resultConsumer;

   public StepResult execute()
   {
      StepResult stepResult;

      try
      {
         ArrayList<PreparedStep> nestedSteps = step.getSteps();

         if ( ( nestedSteps == null ) || nestedSteps.isEmpty() )
         {
            // TODO:Handle null exceptions
            StepRunner stepRunner = kartaRuntime.getStepRunner( runInfo );
            stepResult = stepRunner.runStep( step );
         }
         else
         {
            // TODO: Forward test data and test data set from parent step to nested steps
            Boolean runInParallel = step.getRunStepsInParallel();

            if ( ( runInParallel != null ) && runInParallel )
            {
               int        numberOfParallelSteps = nestedSteps.size();
               StepResult cumulativeStepResult  = new StepResult();
               cumulativeStepResult.setStartTime( new Date() );
               ExecutorService stepExecutorService = new ThreadPoolExecutor( numberOfParallelSteps, numberOfParallelSteps, 0L, TimeUnit.MILLISECONDS,
                        new BlockingRunnableQueue( numberOfParallelSteps ) );

               for ( PreparedStep nestedStep : nestedSteps )
               {
                  PreparedStepRunner preparedStepRunner = PreparedStepRunner.builder().kartaRuntime( kartaRuntime ).runInfo( runInfo )
                           .step( nestedStep ).resultConsumer( cumulativeStepResult::mergeResults ).build();
                  stepExecutorService.submit( preparedStepRunner );
               }

               stepExecutorService.shutdown();
               try
               {
                  // TODO: Change to WaitUtil impl with max timeout
                  if ( !stepExecutorService.awaitTermination( Long.MAX_VALUE, TimeUnit.SECONDS ) )
                  {
                     log.error( "Failed awaiting termination of step executor service" );
                  }
               }
               catch ( InterruptedException ie )
               {
                  // Nothing to do
                  if ( !stepExecutorService.isTerminated() )
                  {
                     log.warn( "Wait for parallel step threads was interrupted ", ie );
                  }
               }
               stepResult = cumulativeStepResult;
            }
            else
            {
               stepResult = new StepResult();
               for ( PreparedStep nestedStep : nestedSteps )
               {
                  PreparedStepRunner preparedStepRunner = PreparedStepRunner.builder().kartaRuntime( kartaRuntime ).runInfo( runInfo )
                           .step( nestedStep ).build();
                  stepResult.mergeResults( preparedStepRunner.call() );

                  if ( !stepResult.isPassed() )
                  {
                     return stepResult;
                  }
               }
            }
         }
      }
      catch ( TestFailureException t )
      {
         stepResult = StandardStepResults.failure( t );
      }

      return stepResult;
   }

   @Override
   public StepResult call()
   {
      Date       startTime = new Date();
      StepResult stepResult;//= new StepResult();

      try
      {
         stepResult = execute();

         if ( stepResult.isFailed() )
         {
            for ( int currentRetry = 0; currentRetry < step.getMaxRetries(); currentRetry++ )
            {
               StepResult retryResult = execute();
               stepResult.mergeResults( retryResult );

               if ( !retryResult.isFailed() )
               {
                  break;
               }
            }
         }
      }
      catch ( Throwable t )
      {
         stepResult = StandardStepResults.error( t );
      }

      stepResult.setStartTime( startTime );

      if ( resultConsumer != null )
      {
         resultConsumer.accept( stepResult );
      }

      return stepResult;
   }
}
