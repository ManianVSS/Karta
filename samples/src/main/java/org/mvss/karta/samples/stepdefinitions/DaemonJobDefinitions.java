package org.mvss.karta.samples.stepdefinitions;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.core.StepDefinition;

@Log4j2
public class DaemonJobDefinitions
{
   @StepDefinition( "monitor the calculator" )
   public void monitor_the_calculator()
   {
      try
      {
         //noinspection InfiniteLoopStatement
         for ( long i = 0; ; i++ )
         {
            log.info( "Calculator is on. Iteration number " + i );
            //noinspection BusyWait
            Thread.sleep( 100 );
         }
      }
      catch ( InterruptedException ie )
      {
         log.info( "Stopping calculator monitoring job." );
      }
   }
}
