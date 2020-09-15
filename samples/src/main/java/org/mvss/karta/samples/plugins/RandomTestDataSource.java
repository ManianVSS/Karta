package org.mvss.karta.samples.plugins;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;

import lombok.Getter;

public class RandomTestDataSource implements TestDataSource
{
   @Getter
   private final String pluginName = "RandomTestDataSource";

   private Random       random;

   @Override
   public boolean initialize( HashMap<String, Serializable> properties ) throws Throwable
   {
      if ( properties.containsKey( "seed" ) )
      {
         random = new Random( (int) properties.get( "seed" ) );
      }
      else
      {
         random = new Random();
      }

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
