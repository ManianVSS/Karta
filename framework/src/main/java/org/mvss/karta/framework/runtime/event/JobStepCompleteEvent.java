package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class JobStepCompleteEvent extends JobEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JobStepCompleteEvent( String runName, String featureName, TestJob job, long iterationNumber, TestStep step, StepResult result )
   {
      super( StandardEventsTypes.JOB_STEP_COMPLETE_EVENT, runName, featureName, job, iterationNumber );
      this.parameters.put( Constants.STEP, step );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public TestStep getStep()
   {
      return (TestStep) parameters.get( Constants.STEP );
   }

   @JsonIgnore
   public StepResult getResult()
   {
      return (StepResult) parameters.get( Constants.RESULT );
   }
}
