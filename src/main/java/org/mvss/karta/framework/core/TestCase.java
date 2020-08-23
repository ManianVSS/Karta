package org.mvss.karta.framework.core;

import java.io.Serializable;

import lombok.extern.log4j.Log4j;

@Log4j
public interface TestCase extends Serializable
{
   default void beforeTest()
   {

   }

   void runTest();

   default void afterTest()
   {

   }
}
