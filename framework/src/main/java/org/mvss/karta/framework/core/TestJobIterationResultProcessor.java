package org.mvss.karta.framework.core;

@FunctionalInterface
public interface TestJobIterationResultProcessor
{
   void consume( String jobName, TestJobResult testJobResult );
}
