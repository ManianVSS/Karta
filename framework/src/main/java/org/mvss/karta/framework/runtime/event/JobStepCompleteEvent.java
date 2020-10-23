package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestStep;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class JobStepCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;
   private TestJob           job;
   private long              iterationNumber;
   private TestStep          step;
   private StepResult        result;

   public JobStepCompleteEvent( String runName, TestFeature feature, TestJob job, long iterationNumber, TestStep jobStep, StepResult result )
   {
      super( StandardEventsTypes.JOB_STEP_COMPLETE_EVENT, runName );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.job = job;
      this.step = jobStep;
      this.result = result;
   }

   @Builder
   public JobStepCompleteEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, TestJob job, long iterationNumber, TestStep step, StepResult result )
   {
      super( StandardEventsTypes.JOB_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.job = job;
      this.step = step;
      this.result = result;
   }
}
