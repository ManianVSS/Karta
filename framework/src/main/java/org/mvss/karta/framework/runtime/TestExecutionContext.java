package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.interfaces.TestDataSource;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//TODO: Thread specific bean registry to share control objects for a thread/runtime level
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestExecutionContext implements Serializable
{

   /**
    * 
    */
   private static final long             serialVersionUID = 1L;

   private String                        runName;
   private String                        featureName;

   @Builder.Default
   private long                          iterationIndex   = -1;

   private String                        scenarioName;
   private String                        stepIdentifier;

   private HashMap<String, Serializable> data;

   @Builder.Default
   private HashMap<String, Serializable> variables        = new HashMap<String, Serializable>();

   @JsonIgnore
   private transient BeanRegistry        contextBeanRegistry;

   public TestExecutionContext( String runName, String featureName, long iterationIndex, String scenarioName, String stepIdentifier, HashMap<String, Serializable> data, HashMap<String, Serializable> variables )
   {
      super();
      this.runName = runName;
      this.featureName = featureName;
      this.iterationIndex = iterationIndex;
      this.scenarioName = scenarioName;
      this.stepIdentifier = stepIdentifier;
      this.data = data;
      this.variables = variables;
   }

   public void mergeTestData( HashMap<String, Serializable> stepTestData, HashMap<String, ArrayList<Serializable>> testDataSet, ArrayList<TestDataSource> testDataSources ) throws Throwable
   {
      data = new HashMap<String, Serializable>();

      if ( testDataSources != null )
      {
         for ( TestDataSource tds : testDataSources )
         {
            HashMap<String, Serializable> testData = tds.getData( this );
            testData.forEach( ( key, value ) -> data.put( key, value ) );
         }
      }

      long iterationIndexForData = ( this.iterationIndex < 0 ) ? 0 : this.iterationIndex;

      if ( testDataSet != null )
      {
         for ( String dataKey : testDataSet.keySet() )
         {
            ArrayList<Serializable> possibleValues = testDataSet.get( dataKey );
            if ( ( possibleValues != null ) && !possibleValues.isEmpty() )
            {
               int valueIndex = (int) ( iterationIndexForData % possibleValues.size() );
               data.put( dataKey, possibleValues.get( valueIndex ) );
            }
         }
      }

      if ( stepTestData != null )
      {
         stepTestData.forEach( ( key, value ) -> data.put( key, value ) );
      }
   }
}
