package org.mvss.karta.framework.core;

/**
 * This functional interface allows processing of result of a test job's iteration typically used for report accumulation
 *
 * @author Manian
 */
@FunctionalInterface
public interface TestJobIterationResultProcessor
{
   void consume( String jobName, TestJobResult testJobResult );
}
