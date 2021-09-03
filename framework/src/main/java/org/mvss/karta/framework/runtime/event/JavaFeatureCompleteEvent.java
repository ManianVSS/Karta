package org.mvss.karta.framework.runtime.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class JavaFeatureCompleteEvent extends FeatureEvent
{
   private static final long serialVersionUID = 1L;

   public JavaFeatureCompleteEvent( Event event )
   {
      super( event );
      parameters.put( Constants.RESULT, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.RESULT ), FeatureResult.class ) );
   }

   public JavaFeatureCompleteEvent( String runName, String featureName, FeatureResult result )
   {
      super( StandardEventsTypes.JAVA_FEATURE_COMPLETE_EVENT, runName, featureName );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public FeatureResult getResult()
   {
      return (FeatureResult) parameters.get( Constants.RESULT );
   }
}
