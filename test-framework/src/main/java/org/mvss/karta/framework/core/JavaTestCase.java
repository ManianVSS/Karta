package org.mvss.karta.framework.core;

public interface JavaTestCase
{
   default void beforeTest()
   {
      System.out.println( this.getClass().getCanonicalName() + ": Before test " );
   }

   default void beforeIteration()
   {

   }

   void runIteration();

   default void afterIteration()
   {

   }

   default void runTest()
   {
      beforeIteration();
      runIteration();
      afterIteration();
   }

   default void afterTest()
   {
      System.out.println( this.getClass().getCanonicalName() + ": After test " );
   }
}
