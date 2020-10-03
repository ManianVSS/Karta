package org.mvss.karta.samples.plugins;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import org.mvss.karta.framework.runtime.Configurator;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;

import lombok.Getter;

public class RandomTestDataSource implements TestDataSource
{
   @Getter
   private static final String PLUGIN_NAME = "RandomTestDataSource";

   @PropertyMapping( group = PLUGIN_NAME, value = "seed" )
   private Integer             seed        = null;

   private Random              random;

   private boolean             initialized = false;

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   @Override
   public boolean initialize( HashMap<String, HashMap<String, Serializable>> properties ) throws Throwable
   {
      if ( initialized )
      {
         return true;
      }

      Configurator.loadProperties( properties, this );

      if ( seed != null )
      {
         random = new Random( seed );
      }
      else
      {
         random = new Random();
      }

      initialized = true;
      return true;
   }

   @Override
   public void close() throws Exception
   {

   }

   @Override
   public HashMap<String, Serializable> getData( ExecutionStepPointer executionStepPointer ) throws Throwable
   {
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
      testData.put( "randomInt", random.nextInt() );
      return testData;
   }

}
