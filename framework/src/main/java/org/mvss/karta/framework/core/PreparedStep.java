package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import org.mvss.karta.framework.runtime.TestExecutionContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Prepared step for execution
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class PreparedStep implements Serializable
{
   private static final long       serialVersionUID          = 1L;

   /**
    * The step identifier.
    */
   private String                  identifier;

   /**
    * The test execution context object for running the step.
    */
   private TestExecutionContext    testExecutionContext;

   /**
    * The remote node on which to run the step on.
    */
   private String                  node;

   /**
    * Indicates if the same step is to be run in multiple threads in parallel.
    */
   @Builder.Default
   private int                     numberOfThreadsInParallel = 1;

   /**
    * The group of prepared steps to run.
    */
   private ArrayList<PreparedStep> nestedSteps;

   /**
    * Indicates if this is a group of steps to be run in parallel or in sequence.
    */
   private Boolean                 runNestedStepsInParallel;
}
