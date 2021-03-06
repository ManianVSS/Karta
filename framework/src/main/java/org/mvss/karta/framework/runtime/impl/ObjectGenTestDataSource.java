package org.mvss.karta.framework.runtime.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import org.mvss.karta.framework.core.Initializer;
import org.mvss.karta.framework.randomization.ObjectGenerationRule;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ObjectGenTestDataSource implements TestDataSource
{
   public static final String                             PLUGIN_NAME          = "ObjectGenTestDataSource";

   @PropertyMapping( group = PLUGIN_NAME, value = "enableRuleValidation" )
   private boolean                                        enableRuleValidation = true;

   @PropertyMapping( group = PLUGIN_NAME, value = "seed" )
   private Long                                           seed                 = null;

   @PropertyMapping( group = PLUGIN_NAME, value = "objectRuleMap" )
   private HashMap<String, HashMap<String, Serializable>> objectRuleMapRaw     = new HashMap<String, HashMap<String, Serializable>>();

   private HashMap<String, ObjectGenerationRule>          objectRuleMap        = new HashMap<String, ObjectGenerationRule>();

   private Random                                         random;

   private boolean                                        initialized          = false;

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   @Initializer
   public boolean initialize() throws Throwable
   {
      if ( initialized )
      {
         return true;
      }
      log.info( "Initializing " + PLUGIN_NAME + " plugin" );
      random = ( seed == null ) ? new Random() : new Random( seed );

      ObjectMapper objectMapper = ParserUtils.getObjectMapper();

      for ( String objectKey : objectRuleMapRaw.keySet() )
      {
         ObjectGenerationRule ruleToAdd = objectMapper.readValue( objectMapper.writeValueAsString( objectRuleMapRaw.get( objectKey ) ), ObjectGenerationRule.class );

         if ( enableRuleValidation && !ruleToAdd.validateConfiguration() )
         {
            log.error( "Object rule generation failed validation: " + ruleToAdd );
            continue;
         }

         objectRuleMap.put( objectKey, ruleToAdd );
      }

      initialized = true;
      return true;
   }

   @Override
   public HashMap<String, Serializable> getData( TestExecutionContext testExecutionContext ) throws Throwable
   {
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();

      for ( String objectRuleName : objectRuleMap.keySet() )
      {
         ObjectGenerationRule objectRule = objectRuleMap.get( objectRuleName );
         testData.put( objectRuleName, objectRule.generateNextValue( random ) );
      }
      return testData;
   }

}
