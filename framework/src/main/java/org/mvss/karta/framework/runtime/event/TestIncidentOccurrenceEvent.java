package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestIncident;
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
public class TestIncidentOccurrenceEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public TestIncidentOccurrenceEvent( String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier, TestIncident incident )
   {
      super( StandardEventsTypes.TEST_INCIDENT_OCCURRENCE_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
      this.parameters.put( Constants.INCIDENT, incident );
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
