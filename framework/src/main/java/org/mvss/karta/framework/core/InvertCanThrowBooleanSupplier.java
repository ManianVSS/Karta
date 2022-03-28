package org.mvss.karta.framework.core;

import java.util.function.BooleanSupplier;

/**
 * Inverted boolean supplier for a given condition
 */
public class InvertCanThrowBooleanSupplier implements BooleanSupplier, CanThrowBooleanSupplier
{

   private CanThrowBooleanSupplier canThrowBooleanSupplier;
   private BooleanSupplier         booleanSupplier;

   public InvertCanThrowBooleanSupplier( CanThrowBooleanSupplier canThrowBooleanSupplier )
   {
      this.canThrowBooleanSupplier = canThrowBooleanSupplier;
   }

   public InvertCanThrowBooleanSupplier( BooleanSupplier booleanSupplier )
   {
      this.booleanSupplier = booleanSupplier;
   }

   @Override
   public boolean evaluate() throws Throwable
   {
      return !canThrowBooleanSupplier.evaluate();
   }

   @Override
   public boolean getAsBoolean()
   {
      return !booleanSupplier.getAsBoolean();
   }

}
