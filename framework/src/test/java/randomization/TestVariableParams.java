package randomization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.mvss.karta.framework.randomization.DistributionType;
import org.mvss.karta.framework.randomization.ParameterType;
import org.mvss.karta.framework.randomization.ProbableData;
import org.mvss.karta.framework.randomization.Range;
import org.mvss.karta.framework.randomization.VariableParameter;

public class TestVariableParams
{
   public static void testVariableParam( VariableParameter vp, int iterations, boolean printObj ) throws Exception
   {
      HashMap<Object, Integer> objCount = new HashMap<Object, Integer>();

      for ( int i = 0; i < iterations; i++ )
      {
         Object nextObject = vp.generateNextValue();

         if ( nextObject == null )
         {
            throw new Exception( "Null value from variable generator" );
         }

         Integer currentCount = objCount.get( nextObject );

         if ( currentCount == null )
         {
            currentCount = 0;
         }

         currentCount++;
         objCount.put( nextObject, currentCount );
      }

      for ( Object obj : objCount.keySet() )
      {
         System.out.println( ( printObj ? "Obj: " + ( obj.getClass().isArray() ? Arrays.toString( (Object[]) obj ) : obj.toString() ) : "-> Object" ) + " frequency is " + objCount.get( obj ) * 100.0 / iterations + "%" );
      }
   }

   public static void main( String[] args )
   {
      try
      {
         System.out.println( "Demo random distribution for long" );
         VariableParameter longVP = VariableParameter.builder().variableName( "LongVar" ).parameterType( ParameterType.Long ).distributionType( DistributionType.RANDOM ).range( new Range( 900l, 1000l ) ).build();
         testVariableParam( longVP, 1000000, true );

         System.out.println( "Demo random distribution for String" );
         VariableParameter stringVP = VariableParameter.builder().variableName( "StringVar" ).parameterType( ParameterType.String ).distributionType( DistributionType.RANDOM ).range( new Range( 1024000l, 2048000l ) ).build();
         testVariableParam( stringVP, 10, false );

         System.out.println( "Demo random distribution for String Values" );
         ArrayList<ProbableData> stringValueProbDist = new ArrayList<ProbableData>();
         stringValueProbDist.add( new ProbableData( 66, "One", "First" ) );
         stringValueProbDist.add( new ProbableData( 22, "Two", "Second" ) );
         stringValueProbDist.add( new ProbableData( 12, "Three", "Third" ) );
         VariableParameter stringValuesVP = VariableParameter.builder().variableName( "StringValuesVar" ).parameterType( ParameterType.String ).distributionType( DistributionType.PROBABILITY_DISTRIBUTION ).probabilityDistMap( stringValueProbDist ).build();
         testVariableParam( stringValuesVP, 1000000, true );

         System.out.println( "Demo random distribution for cricket scores" );
         ArrayList<ProbableData> cricketScoreProbDist = new ArrayList<ProbableData>();
         cricketScoreProbDist.add( new ProbableData( 5, 0 ) );
         cricketScoreProbDist.add( new ProbableData( 5, new Range( 1, 29 ) ) );
         cricketScoreProbDist.add( new ProbableData( 40, new Range( 30, 69 ) ) );
         cricketScoreProbDist.add( new ProbableData( 15, new Range( 70, 95 ) ) );
         cricketScoreProbDist.add( new ProbableData( 10, 96, 97, 98, 99 ) );
         cricketScoreProbDist.add( new ProbableData( 25, new Range( 100, 150 ) ) );
         VariableParameter cricketScoreVP = VariableParameter.builder().variableName( "cricketScoreVar" ).parameterType( ParameterType.Integer ).distributionType( DistributionType.PROBABILITY_DISTRIBUTION ).probabilityDistMap( cricketScoreProbDist )
                  .build();
         testVariableParam( cricketScoreVP, 1000000, true );

         System.out.println( "Demo random distribution for variable set" );
         longVP.setProbabilityOfOccurence( 66 );
         stringVP.setProbabilityOfOccurence( 66 );
         VariableParameter variableSet1 = VariableParameter.builder().variableName( "variableSet1" ).parameterType( ParameterType.VariableSet ).build().addVariableParams( longVP, stringValuesVP );
         testVariableParam( variableSet1, 1000000, true );

         System.out.println( "Demo random distribution for mutex variable in set" );
         variableSet1.setProbabilityOfOccurence( 60 );
         cricketScoreVP.setProbabilityOfOccurence( 40 );
         VariableParameter variableSet2 = VariableParameter.builder().variableName( "variableSet2" ).parameterType( ParameterType.MutexVariableInSet ).build().addVariableParams( variableSet1, cricketScoreVP );
         testVariableParam( variableSet2, 1000000, true );

      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }
   }
}
