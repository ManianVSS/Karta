package org.mvss.karta.framework.randomization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.utils.DataUtils;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectGenerationRule implements Serializable, ObjectWithChance
{

   /**
    * 
    */
   private static final long               serialVersionUID                = 1L;

   private static final long               STRING_LENGTH_RANGE_DEFAULT_MAX = 100;
   private static final long               BINARY_DATA_RANGE_DEFAULT_MAX   = 1024;

   private String                          fieldName;

   @Builder.Default
   private ObjectGenerationRuleType        ruleType                        = ObjectGenerationRuleType.VALUES;

   private Range                           range;

   private ArrayList<Serializable>         values;

   @Builder.Default
   private float                           probability                     = 1;

   private ArrayList<ObjectGenerationRule> objectRules;

   public boolean checkIfSetType()
   {
      return ( ruleType == ObjectGenerationRuleType.OBJECT_RULE ) || ( ruleType == ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE );
   }

   public boolean validateConfiguration()
   {
      if ( ( ruleType == ObjectGenerationRuleType.OBJECT_RULE ) || ( ruleType == ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE ) )
      {
         if ( ( objectRules == null ) || ( objectRules.isEmpty() ) )
         {
            return false;
         }

         for ( ObjectGenerationRule variableField : objectRules )
         {
            // Variable names can't be blank for variable object
            if ( ( variableField.ruleType == ObjectGenerationRuleType.OBJECT_RULE ) && StringUtils.isBlank( fieldName ) )
            {
               return false;
            }

            if ( !variableField.validateConfiguration() )
            {
               return false;
            }
         }

         return ( ruleType == ObjectGenerationRuleType.MUTEX_OBJECT_RULE_VALUE ) ? RandomizationUtils.checkForProbabilityCoverage( objectRules ) : true;
      }

      return true;
   }

   public ObjectGenerationRule addVariableParams( ObjectGenerationRule... variableParamsToAdd )
   {
      if ( objectRules == null )
      {
         objectRules = new ArrayList<ObjectGenerationRule>();
      }

      for ( ObjectGenerationRule variableParam : variableParamsToAdd )
      {
         if ( ( variableParam != null ) && !objectRules.contains( variableParam ) )
         {
            objectRules.add( variableParam );
         }
      }

      return this;
   }

   public Serializable generateNextValue( Random random )
   {
      try
      {
         switch ( ruleType )
         {
            case OBJECT_RULE:
               if ( objectRules == null )
               {
                  return null;
               }

               HashMap<String, Serializable> variableMap = new HashMap<String, Serializable>();
               ArrayList<ObjectGenerationRule> selectedVariables = RandomizationUtils.generateNextComposition( random, objectRules );
               for ( ObjectGenerationRule variableParam : selectedVariables )
               {
                  variableMap.put( variableParam.fieldName, variableParam.generateNextValue( random ) );
               }

               return variableMap;

            case MUTEX_OBJECT_RULE_VALUE:
               ObjectGenerationRule variableParam = RandomizationUtils.generateNextMutexComposition( random, objectRules );
               return ( variableParam == null ) ? null : variableParam.generateNextValue( random );

            case VALUES:
               if ( ( values == null ) || values.isEmpty() )
               {
                  return null;
               }
               if ( values.size() == 1 )
               {
                  return values.get( 0 );
               }
               else
               {
                  int randomIndex = random.nextInt( values.size() );
                  return values.get( randomIndex );
               }

            case UUID:
               return UUID.randomUUID();

            case BOOLEAN:
               return random.nextBoolean();

            case INTEGER_RANGE:
               if ( range == null )
               {
                  range = new Range( 0, Integer.MAX_VALUE );
               }
               return (int) range.getNext( random );

            case LONG_RANGE:
               if ( range == null )
               {
                  range = new Range();
               }
               return range.getNext( random );

            case STRING_RANGE:
               if ( range == null )
               {
                  range = new Range( 1, STRING_LENGTH_RANGE_DEFAULT_MAX );
               }
               return DataUtils.randomAlphaNumericString( random, (int) range.getNext( random ) );

            case BINARY_DATA_RANGE:
               if ( range == null )
               {
                  range = new Range( 1, BINARY_DATA_RANGE_DEFAULT_MAX );
               }
               int length = (int) range.getNext( random );

               if ( length == 0 )
               {
                  return null;
               }

               byte[] toReturn = new byte[length];
               random.nextBytes( toReturn );
               return Base64Coder.encode( toReturn );

            default:
               return null;
         }
      }
      catch ( Throwable t )
      {
         return null;
      }
   }
}
