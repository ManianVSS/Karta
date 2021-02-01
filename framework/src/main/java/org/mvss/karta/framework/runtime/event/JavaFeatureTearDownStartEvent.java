package org.mvss.karta.framework.runtime.event;

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
public class JavaFeatureTearDownStartEvent extends FeatureEvent
{
   private static final long serialVersionUID = 1L;

   public JavaFeatureTearDownStartEvent( Event event )
   {
      super( event );
      parameters.put( Constants.STEP_IDENTIFIER, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.STEP_IDENTIFIER ), String.class ) );
   }

   public JavaFeatureTearDownStartEvent( String runName, String featureName, String stepIdentifier )
   {
      super( StandardEventsTypes.JAVA_FEATURE_TEARDOWN_START_EVENT, runName, featureName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
   }

   @JsonIgnore
   public String getStepIdentifier()
   {
      return parameters.get( Constants.STEP_IDENTIFIER ).toString();
   }
}
