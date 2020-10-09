package randomization;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.mvss.karta.framework.randomization.ObjectGenerationRule;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestVariableParams
{
   public static boolean testVariableParam( Random random, ObjectGenerationRule vp, int iterations, boolean printObj ) throws Exception
   {
      HashMap<Serializable, Integer> objCount = new HashMap<Serializable, Integer>();

      for ( int i = 0; i < iterations; i++ )
      {
         Serializable nextObject = vp.generateNextValue( random );

         while ( nextObject == null )
         {
            return false;
         }

         Integer currentCount = objCount.get( nextObject );

         if ( currentCount == null )
         {
            currentCount = 0;
         }

         currentCount++;
         objCount.put( nextObject, currentCount );
      }

      for ( Serializable obj : objCount.keySet() )
      {
         System.out.println( ( printObj ? "Obj: " + ( obj.getClass().isArray() ? Arrays.toString( (Object[]) obj ) : obj.toString() ) : "-> Object" ) + " frequency is " + objCount.get( obj ) * 100.0 / iterations + "%" );
      }
      return true;
   }

   public static void main( String[] args )
   {
      try
      {
         Random random = new Random();
         // System.out.println( "Demo random distribution for long" );
         // VariableObject longVP = VariableObject.builder().variableName( "LongVar" ).parameterType( ParameterType.LONG_RANGE ).range( new Range( 900l, 1000l ) ).build();
         // testVariableParam( random, longVP, 1000000, true );
         //
         // System.out.println( "Demo random distribution for String" );
         // VariableObject stringVP = VariableObject.builder().variableName( "StringVar" ).parameterType( ParameterType.STRING_RANGE ).range( new Range( 10, 20 ) ).build();
         // testVariableParam( random, stringVP, 10, true );
         //
         // System.out.println( "Demo random distribution for String Values" );
         // ArrayList<VariableObject> stringValueProbDist = new ArrayList<VariableObject>();
         // stringValueProbDist.add( VariableObject.builder().probabilityOfOccurrence( 0.66f ).values( new ArrayList<Serializable>( Arrays.asList( "One", "First" ) ) ).build() );
         // stringValueProbDist.add( VariableObject.builder().probabilityOfOccurrence( 0.22f ).values( new ArrayList<Serializable>( Arrays.asList( "Two", "Second" ) ) ).build() );
         // stringValueProbDist.add( VariableObject.builder().probabilityOfOccurrence( 0.12f ).values( new ArrayList<Serializable>( Arrays.asList( "Three", "Third" ) ) ).build() );
         //
         // VariableObject stringValuesVP = VariableObject.builder().variableName( "StringValuesVar" ).parameterType( ParameterType.MUTEX_VARIABLE_OBJECT ).variableMembers( stringValueProbDist ).build();
         // testVariableParam( random, stringValuesVP, 1000000, true );
         //
         // System.out.println( "Demo random distribution for cricket scores" );
         // ArrayList<VariableObject> cricketScoreProbDist = new ArrayList<VariableObject>();
         // cricketScoreProbDist.add( VariableObject.builder().probabilityOfOccurrence( 0.05f ).values( new ArrayList<Serializable>( Arrays.asList( 0 ) ) ).build() );
         // cricketScoreProbDist.add( VariableObject.builder().probabilityOfOccurrence( 0.05f ).parameterType( ParameterType.INTEGER_RANGE ).range( new Range( 1, 29 ) ).build() );
         // cricketScoreProbDist.add( VariableObject.builder().probabilityOfOccurrence( 0.40f ).parameterType( ParameterType.INTEGER_RANGE ).range( new Range( 30, 69 ) ).build() );
         // cricketScoreProbDist.add( VariableObject.builder().probabilityOfOccurrence( 0.15f ).parameterType( ParameterType.INTEGER_RANGE ).range( new Range( 70, 95 ) ).build() );
         // cricketScoreProbDist.add( VariableObject.builder().probabilityOfOccurrence( 0.10f ).values( new ArrayList<Serializable>( Arrays.asList( 96, 97, 98, 99 ) ) ).build() );
         // cricketScoreProbDist.add( VariableObject.builder().probabilityOfOccurrence( 0.25f ).parameterType( ParameterType.INTEGER_RANGE ).range( new Range( 100, 150 ) ).build() );
         // VariableObject cricketScoreVP = VariableObject.builder().variableName( "cricketScoreVar" ).parameterType( ParameterType.MUTEX_VARIABLE_OBJECT ).variableMembers( cricketScoreProbDist ).build();
         // testVariableParam( random, cricketScoreVP, 1000000, true );

         // System.out.println( "Demo random distribution for mutex variable in set" );
         // longVP.setProbabilityOfOccurrence( 0.60f );
         // stringValuesVP.setProbabilityOfOccurrence( 0.40f );
         // VariableObject mutexVarSet = VariableObject.builder().variableName( "mutexVarSet" ).parameterType( ParameterType.MUTEX_VARIABLE_OBJECT ).build().addVariableParams( longVP, stringValuesVP );
         // testVariableParam( random, mutexVarSet, 1000000, true );
         //
         // System.out.println( "Demo random distribution for variable set" );
         // mutexVarSet.setProbabilityOfOccurrence( 0.66f );
         // cricketScoreVP.setProbabilityOfOccurrence( 0.66f );
         // VariableObject varSet = VariableObject.builder().variableName( "varSet" ).parameterType( ParameterType.VARIABLE_OBJECT ).build().addVariableParams( mutexVarSet, cricketScoreVP );
         // testVariableParam( random, varSet, 1000000, true );

         ObjectMapper yamlParser = ParserUtils.getYamlObjectMapper();
         System.out.println( "Demo random distribution for employee object" );
         ObjectGenerationRule employeeObjFromFile = yamlParser.readValue( FileUtils.readFileToString( new File( "SampleVariableObject.yaml" ), Charset.defaultCharset() ), ObjectGenerationRule.class );
         // testVariableParam( random, employeeObjFromFile, 100, true );

         for ( int i = 0; i < 200; i++ )
         {
            Employee nextObject = yamlParser.convertValue( employeeObjFromFile.generateNextValue( random ), Employee.class );
            System.out.println( nextObject );
         }

      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }
}
