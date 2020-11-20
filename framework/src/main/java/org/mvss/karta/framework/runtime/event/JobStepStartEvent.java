package org.mvss.karta.framework.runtime.event;

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
public class JobStepStartEvent extends JobEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JobStepStartEvent( String runName, String featureName, TestJob job, long iterationNumber, TestStep step )
   {
      super( StandardEventsTypes.JOB_STEP_START_EVENT, runName, featureName, job, iterationNumber );
      this.parameters.put( Constants.STEP, step );
   }

   @JsonIgnore
   public TestStep getStep()
   {
      return (TestStep) parameters.get( Constants.STEP );
   }
}
