package randomization;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.mvss.karta.framework.randomization.ObjectGenerationRule;
import org.mvss.karta.framework.randomization.ObjectGenerationRuleType;
import org.mvss.karta.framework.randomization.Range;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestObjectGenerationRule
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
         System.out.println( "Demo random distribution for long" );
         ObjectGenerationRule longVP = ObjectGenerationRule.builder().fieldName( "LongVar" ).ruleType( ObjectGenerationRuleType.LONG_RANGE ).range( new Range( 900l, 1000l ) ).build();
         testVariableParam( random, longVP, 1000000, true );

         System.out.println( "Demo random distribution for String" );
         ObjectGenerationRule stringVP = ObjectGenerationRule.builder().fieldName( "StringVar" ).ruleType( ObjectGenerationRuleType.STRING_RANGE ).range( new Range( 10, 20 ) ).build();
         testVariableParam( random, stringVP, 10, true );

         System.out.println( "Demo random distribution for String Values" );
         ArrayList<ObjectGenerationRule> stringValueProbDist = new ArrayList<ObjectGenerationRule>();
         stringValueProbDist.add( ObjectGenerationRule.builder().probability( 0.66f ).values( new ArrayList<Serializable>( Arrays.asList( "One", "First" ) ) ).build() );
         stringValueProbDist.add( ObjectGenerationRule.builder().probability( 0.22f ).values( new ArrayList<Serializable>( Arrays.asList( "Two", "Second" ) ) ).build() );
         stringValueProbDist.add( ObjectGenerationRule.builder().probability( 0.12f ).values( new ArrayList<Serializable>( Arrays.asList( "Three", "Third" ) ) ).build() );

         ObjectGenerationRule stringValuesVP = ObjectGenerationRule.builder().fieldName( "StringValuesVar" ).ruleType( ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE ).objectRules( stringValueProbDist ).build();
         testVariableParam( random, stringValuesVP, 1000000, true );

         System.out.println( "Demo random distribution for cricket scores" );
         ArrayList<ObjectGenerationRule> cricketScoreProbDist = new ArrayList<ObjectGenerationRule>();
         cricketScoreProbDist.add( ObjectGenerationRule.builder().probability( 0.05f ).values( new ArrayList<Serializable>( Arrays.asList( 0 ) ) ).build() );
         cricketScoreProbDist.add( ObjectGenerationRule.builder().probability( 0.05f ).ruleType( ObjectGenerationRuleType.INTEGER_RANGE ).range( new Range( 1, 29 ) ).build() );
         cricketScoreProbDist.add( ObjectGenerationRule.builder().probability( 0.40f ).ruleType( ObjectGenerationRuleType.INTEGER_RANGE ).range( new Range( 30, 69 ) ).build() );
         cricketScoreProbDist.add( ObjectGenerationRule.builder().probability( 0.15f ).ruleType( ObjectGenerationRuleType.INTEGER_RANGE ).range( new Range( 70, 95 ) ).build() );
         cricketScoreProbDist.add( ObjectGenerationRule.builder().probability( 0.10f ).values( new ArrayList<Serializable>( Arrays.asList( 96, 97, 98, 99 ) ) ).build() );
         cricketScoreProbDist.add( ObjectGenerationRule.builder().probability( 0.25f ).ruleType( ObjectGenerationRuleType.INTEGER_RANGE ).range( new Range( 100, 150 ) ).build() );
         ObjectGenerationRule cricketScoreVP = ObjectGenerationRule.builder().fieldName( "cricketScoreVar" ).ruleType( ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE ).objectRules( cricketScoreProbDist ).build();
         testVariableParam( random, cricketScoreVP, 1000000, true );

         System.out.println( "Demo random distribution for mutex variable in set" );
         longVP.setProbability( 0.60f );
         stringValuesVP.setProbability( 0.40f );
         ObjectGenerationRule mutexVarSet = ObjectGenerationRule.builder().fieldName( "mutexVarSet" ).ruleType( ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE ).build().addVariableParams( longVP, stringValuesVP );
         testVariableParam( random, mutexVarSet, 1000000, true );

         System.out.println( "Demo random distribution for variable set" );
         mutexVarSet.setProbability( 0.66f );
         cricketScoreVP.setProbability( 0.66f );
         ObjectGenerationRule varSet = ObjectGenerationRule.builder().fieldName( "varSet" ).ruleType( ObjectGenerationRuleType.OBJECT_RULE ).build().addVariableParams( mutexVarSet, cricketScoreVP );
         testVariableParam( random, varSet, 1000000, true );

         ObjectMapper yamlParser = ParserUtils.getYamlObjectMapper();
         System.out.println( "Demo random distribution for employee object" );
         ObjectGenerationRule employeeObjFromFile = yamlParser.readValue( FileUtils.readFileToString( new File( "SampleObjectGenerationRule.yaml" ), Charset.defaultCharset() ), ObjectGenerationRule.class );
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
