package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.RunResult;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class RunCompleteEvent extends Event
{
   private static final long serialVersionUID = 1L;

   public RunCompleteEvent( Event event )
   {
      super( event );
      parameters.put( Constants.RESULT, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.RESULT ), RunResult.class ) );
   }

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
