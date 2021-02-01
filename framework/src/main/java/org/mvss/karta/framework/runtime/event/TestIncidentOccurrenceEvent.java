package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.TestExecutionContext;
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
public class TestIncidentOccurrenceEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public TestIncidentOccurrenceEvent( Event event )
   {
      super( event );
      parameters.put( Constants.STEP_IDENTIFIER, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.STEP_IDENTIFIER ), String.class ) );
      parameters.put( Constants.INCIDENT, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.INCIDENT ), TestIncident.class ) );
   }

   public TestIncidentOccurrenceEvent( String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier, TestIncident incident )
   {
      super( StandardEventsTypes.TEST_INCIDENT_OCCURRENCE_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
      this.parameters.put( Constants.INCIDENT, incident );
   }

   public TestIncidentOccurrenceEvent( TestExecutionContext context, TestIncident incident )
   {
      this( context.getRunName(), context.getFeatureName(), context.getIterationIndex(), context.getScenarioName(), context.getStepIdentifier(), incident );
   }

   @JsonIgnore
   public String getStepIdentifier()
   {
      return parameters.get( Constants.STEP_IDENTIFIER ).toString();
   }

   @JsonIgnore
   public TestIncident getIncident()
   {
      return (TestIncident) parameters.get( Constants.INCIDENT );
   }
}
