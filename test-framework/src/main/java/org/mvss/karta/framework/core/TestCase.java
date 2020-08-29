package org.mvss.karta.framework.core;

import java.io.Serializable;

public interface TestCase extends Serializable
{
   default void beforeTest()
   {
      System.out.println( this.getClass().getCanonicalName() + ": Before test " );
   }

   void runTest();

   default void afterTest()
   {
      System.out.println( this.getClass().getCanonicalName() + ": After test " );
   }
}
