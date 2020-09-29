package org.mvss.karta.framework.core;

import org.mvss.karta.framework.runtime.TestExecutionContext;

public interface JavaTestCase
{
   default void beforeTest( TestExecutionContext testExecutionContext )
   {
      System.out.println( this.getClass().getCanonicalName() + ": Before test " );
   }

   default void beforeIteration( TestExecutionContext testExecutionContext )
   {

   }

   void runIteration( TestExecutionContext testExecutionContext );

   default void afterIteration( TestExecutionContext testExecutionContext )
   {

   }

   default void runTest( TestExecutionContext testExecutionContext )
   {
      beforeIteration( testExecutionContext );
      runIteration( testExecutionContext );
      afterIteration( testExecutionContext );
   }

   default void afterTest( TestExecutionContext testExecutionContext )
   {
      System.out.println( this.getClass().getCanonicalName() + ": After test " );
   }
}
