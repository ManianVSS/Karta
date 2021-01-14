package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.RunResult;
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
public class RunCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public RunCompleteEvent( String runName, RunResult result )
   {
      super( StandardEventsTypes.RUN_COMPLETE_EVENT, runName );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public RunResult getResult()
   {
      return (RunResult) parameters.get( Constants.RESULT );
   }
}
