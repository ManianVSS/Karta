package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestJob;
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
public class ChaosActionJobCompleteEvent extends JobEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ChaosActionJobCompleteEvent( String runName, String featureName, TestJob job, long iterationNumber, ChaosAction chaosAction, StepResult result )
   {
      super( StandardEventsTypes.CHAOS_ACTION_JOB_COMPLETE_EVENT, runName, featureName, job, iterationNumber );
      this.parameters.put( Constants.CHAOS_ACTION, chaosAction );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public ChaosAction getChaosAction()
   {
      return (ChaosAction) parameters.get( Constants.CHAOS_ACTION );
   }

   @JsonIgnore
   public StepResult getResult()
   {
      return (StepResult) parameters.get( Constants.RESULT );
   }
}
