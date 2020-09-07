package org.mvss.karta.samples.tests;

import org.mvss.karta.framework.core.JavaTestCase;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Test1 implements JavaTestCase
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @Override
   public void runIteration()
   {
      log.info( "Test iteration" );
   }

}
