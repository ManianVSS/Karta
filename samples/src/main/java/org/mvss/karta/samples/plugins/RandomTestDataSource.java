package org.mvss.karta.samples.plugins;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
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
   public boolean initialize() throws Throwable
   {
      if ( initialized )
      {
         return true;
      }

      log.info( "Initializing " + PLUGIN_NAME + " plugin" );

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
   public HashMap<String, Serializable> getData( TestExecutionContext testExecutionContext ) throws Throwable
   {
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
      testData.put( "randomInt", random.nextInt() );
      return testData;
   }

}
