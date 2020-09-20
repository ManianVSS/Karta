package org.mvss.karta.framework.randomization;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.mvss.karta.framework.runtime.Constants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString( exclude = {"random"} )
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariableParameter implements Serializable
{

   /**
    * 
    */
   private static final long            serialVersionUID            = 1L;

   private static final long            STRING_LENGTH_RANGE_DEFAULT = 102400;

   private String                       variableName;

   @Builder.Default
   private ParameterType                parameterType               = ParameterType.Long;

   @Builder.Default
   private DistributionType             distributionType            = DistributionType.RANDOM;

   private ArrayList<ProbableData>      probabilityDistMap;

   private Range                        range;

   private Object[]                     possibleValues;

   @Builder.Default
   private int                          probabilityOfOccurence      = 100;

   @Builder.Default
   private ArrayList<VariableParameter> variableSetParams           = new ArrayList<VariableParameter>();

   @Builder.Default
   private Random                       random                      = new Random();

   private Object selectValueRandomlyInRange( Range range )
   {
      try
      {
         switch ( parameterType )
         {
            case Boolean:
               return random.nextBoolean();

            case Integer:
               if ( range == null )
               {
                  range = new Range( 0, Integer.MAX_VALUE );
               }
               return (int) range.getNext( random );

            case Long:
               if ( range == null )
               {
                  range = new Range();
               }
               return range.getNext( random );

            case String:
               if ( range == null )
               {
                  range = new Range( 1, STRING_LENGTH_RANGE_DEFAULT );
               }
               int length = (int) range.getNext( random );

               if ( length == 0 )
               {
                  return Constants.EMPTY_STRING;
               }

               byte[] toReturn = new byte[length];
               random.nextBytes( toReturn );
               return new String( toReturn, StandardCharsets.UTF_8 );

            default:
               return null;
         }
      }
      catch ( Throwable t )
      {
         return null;
      }
   }

   private Object selectRandomValueFrom( Object[] values )
   {
      if ( ( values == null ) || ( values.length == 0 ) )
      {
         return null;
      }
      else if ( values.length == 1 )
      {
         return values[0];
      }
      else
      {
         int randomIndex = random.nextInt( values.length );
         return values[randomIndex];
      }
   }

   private Object selectRandomValue( Range range, Object[] values )
   {
      if ( ( values == null ) || ( values.length == 0 ) )
      {
         return selectValueRandomlyInRange( range );
      }
      else
      {
         return selectRandomValueFrom( values );
      }
   }

   public boolean checkIfSetType()
   {
      return ( parameterType == ParameterType.VariableSet ) || ( parameterType == ParameterType.MutexVariableInSet );
   }

   private boolean checkProbabilityDistribution( ArrayList<ProbableData> probabilityDistMap )
   {
      if ( ( probabilityDistMap == null ) || ( probabilityDistMap.size() == 0 ) )
      {
         return false;
      }

      int probabilityNotCovered = 100;

      for ( ProbableData probabilityDist : probabilityDistMap )
      {
         int probabilityOfOccrence = probabilityDist.getProbability();

         if ( ( probabilityOfOccrence <= 0 ) || ( probabilityOfOccrence > 100 ) )
         {
            return false;
         }

         probabilityNotCovered -= probabilityOfOccrence;
      }

      return probabilityNotCovered == 0;
   }

   private boolean checkVariableSet()
   {
      if ( ( variableSetParams == null ) || ( variableSetParams.isEmpty() ) )
      {
         return false;
      }
      int probabilityNotCovered = 100;

      for ( VariableParameter variableParam : variableSetParams )
      {
         if ( !variableParam.validateConfiguration() )
         {
            return false;
         }

         int probabilityOfOccrence = variableParam.getProbabilityOfOccurence();

         if ( ( probabilityOfOccrence <= 0 ) || ( probabilityOfOccrence > 100 ) )
         {
            return false;
         }

         probabilityNotCovered -= probabilityOfOccrence;
      }

      return ( parameterType == ParameterType.MutexVariableInSet ) ? ( probabilityNotCovered == 0 ) : true;
   }

   public boolean validateConfiguration()
   {
      if ( checkIfSetType() )
      {
         if ( !checkVariableSet() )
         {
            return false;
         }
      }
      else
      {
         switch ( distributionType )
         {
            case RANDOM:
               break;

            case PROBABILITY_DISTRIBUTION:
               if ( !checkProbabilityDistribution( probabilityDistMap ) )
               {
                  return false;
               }
               break;
         }
      }

      return true;
   }

   private Object selectRandomlyFromProbabilityDistribution( ArrayList<ProbableData> probabilityDistMap )
   {
      if ( ( probabilityDistMap == null ) || ( probabilityDistMap.isEmpty() ) )
      {
         return null;
      }

      int probabilityNotCovered = 100;
      int randomChance = random.nextInt( 100 ) + 1;
      boolean alreadyPicked = false;
      Object returnValue = null;

      for ( ProbableData probabilityDist : probabilityDistMap )
      {
         if ( probabilityDist.getProbability() < 0 )
         {
            return null;
         }

         int currProbRevCumulative = probabilityNotCovered - Math.min( 100, probabilityDist.getProbability() );

         if ( !alreadyPicked && ( randomChance <= probabilityNotCovered ) && ( randomChance >= currProbRevCumulative ) )
         {
            ArrayList<Object> objectList = new ArrayList<Object>();

            Object[] values = probabilityDist.getValues();

            if ( values != null )
            {
               for ( Object obj : values )
               {
                  if ( obj != null )
                  {
                     objectList.add( obj );
                  }
               }
            }

            returnValue = selectRandomValue( probabilityDist.getRange(), objectList.toArray() );
            alreadyPicked = true;
         }
         probabilityNotCovered = currProbRevCumulative;
      }

      return ( probabilityNotCovered == 0 ) ? returnValue : null;
   }

   public VariableParameter addVariableParams( VariableParameter... variableParamsToAdd )
   {
      for ( VariableParameter variableParam : variableParamsToAdd )
      {
         if ( ( variableParam != null ) && !variableSetParams.contains( variableParam ) )
         {
            variableSetParams.add( variableParam );
         }
      }

      return this;
   }

   private HashMap<String, Object> generateNextComposition()
   {
      HashMap<String, Object> variableMap = new HashMap<String, Object>();

      if ( variableSetParams != null )
      {
         for ( VariableParameter variableParam : variableSetParams )
         {
            int probabilityOfOccurance = variableParam.getProbabilityOfOccurence();

            if ( ( probabilityOfOccurance <= 0 ) || ( probabilityOfOccurance > 100 ) )
            {
               continue;
            }
            if ( ( probabilityOfOccurance == 100 ) || random.nextInt( 100 ) <= probabilityOfOccurance )
            {
               variableMap.put( variableParam.getVariableName(), variableParam.generateNextValue() );
            }
         }
      }
      return variableMap;
   }

   private HashMap<String, Object> generateNextMutexComposition()
   {
      HashMap<String, Object> variableMap = new HashMap<String, Object>();

      ArrayList<ProbableData> mutexVarSetProbDist = new ArrayList<ProbableData>();

      for ( VariableParameter variableParam : variableSetParams )
      {
         mutexVarSetProbDist.add( new ProbableData( variableParam.getProbabilityOfOccurence(), variableParam ) );
      }

      VariableParameter variableParam = (VariableParameter) selectRandomlyFromProbabilityDistribution( mutexVarSetProbDist );

      if ( variableParam != null )
      {
         variableMap.put( variableParam.getVariableName(), variableParam.generateNextValue() );
      }

      return variableMap;
   }

   public synchronized Object generateNextValue()
   {
      switch ( parameterType )
      {
         case UUID:
            return UUID.randomUUID();
         case VariableSet:
            return generateNextComposition();

         case MutexVariableInSet:
            return generateNextMutexComposition();

         case Boolean:
         case Integer:
         case Long:
         case String:
            switch ( distributionType )
            {
               case RANDOM:
                  return selectRandomValue( range, possibleValues );

               case PROBABILITY_DISTRIBUTION:
                  return selectRandomlyFromProbabilityDistribution( probabilityDistMap );
            }

         default:
            return null;

      }
   }
}
