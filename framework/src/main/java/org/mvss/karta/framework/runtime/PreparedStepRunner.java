package org.mvss.karta.framework.runtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

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

   @Override
   public StepResult call()
   {
      StepResult stepResult;
      Date startTime = new Date();

      try
      {
         ArrayList<PreparedStep> nestedSteps = step.getNestedSteps();

         if ( nestedSteps == null )
         {
            // TODO:Handle null exceptions
            StepRunner stepRunner = kartaRuntime.getStepRunner( runInfo );
            stepResult = stepRunner.runStep( step );
         }
         else
         {
            Boolean runInParallel = step.getRunNestedStepsInParallel();

            if ( ( runInParallel != null ) && runInParallel )
            {
               int numberOfParallelSteps = nestedSteps.size();
               StepResult cumulativeStepResult = new StepResult();
               cumulativeStepResult.setStartTime( new Date() );
               ExecutorService stepExecutorService = new ThreadPoolExecutor( numberOfParallelSteps, numberOfParallelSteps, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue( numberOfParallelSteps ) );

               for ( PreparedStep nestedStep : nestedSteps )
               {
                  PreparedStepRunner preparedStepRunner = PreparedStepRunner.builder().kartaRuntime( kartaRuntime ).runInfo( runInfo ).step( nestedStep ).resultConsumer( ( threadStepResult ) -> cumulativeStepResult.mergeResults( threadStepResult ) )
                           .build();
                  stepExecutorService.submit( preparedStepRunner );
               }

               stepExecutorService.shutdown();
               try
               {
                  // TODO: Change to WaitUtil impl with max timeout
                  stepExecutorService.awaitTermination( Long.MAX_VALUE, TimeUnit.SECONDS );
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
                  PreparedStepRunner preparedStepRunner = PreparedStepRunner.builder().kartaRuntime( kartaRuntime ).runInfo( runInfo ).step( nestedStep ).build();
                  stepResult.mergeResults( preparedStepRunner.call() );
               }
            }
         }
      }
      catch ( Throwable t )
      {
         stepResult = StandardStepResults.error( t );
         stepResult.setStartTime( startTime );
      }

      if ( resultConsumer != null )
      {
         resultConsumer.accept( stepResult );
      }

      return stepResult;
   }

}
