package org.mvss.karta.framework.core;

public interface ReliTestCase extends TestCase
{
   default void beforeIteration()
   {

   }

   void runIteration();

   default void afterIteration()
   {

   }
}
