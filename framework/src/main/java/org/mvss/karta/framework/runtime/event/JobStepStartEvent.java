package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

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
public class JobStepStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;
   private TestJob           job;
   private long              iterationNumber;
   private TestStep          step;

   public JobStepStartEvent( String runName, TestFeature feature, TestJob job, long iterationNumber, TestStep jobStep )
   {
      super( StandardEventsTypes.JOB_STEP_START_EVENT, runName );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.job = job;
      this.step = jobStep;
   }

   @Builder
   public JobStepStartEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, TestJob job, long iterationNumber, TestStep jobStep )
   {
      super( StandardEventsTypes.JOB_STEP_START_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.job = job;
      this.step = jobStep;
   }
}
