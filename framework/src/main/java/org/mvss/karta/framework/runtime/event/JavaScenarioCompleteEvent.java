package org.mvss.karta.framework.runtime.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;

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
public class JavaScenarioCompleteEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public JavaScenarioCompleteEvent( Event event )
   {
      super( event );
      parameters.put( Constants.RESULT, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.RESULT ), ScenarioResult.class ) );
   }

   public JavaScenarioCompleteEvent( String runName, String featureName, long iterationNumber, String scenarioName, ScenarioResult result )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_COMPLETE_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public ScenarioResult getResult()
   {
      return (ScenarioResult) parameters.get( Constants.RESULT );
   }
}
