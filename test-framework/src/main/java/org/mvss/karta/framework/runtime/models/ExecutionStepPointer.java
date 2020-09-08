package org.mvss.karta.framework.runtime.models;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExecutionStepPointer implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            testName;
   private String            scenarioName;
   private String            stepDefinition;

   private long              iterationIndex;
   private long              stepIndex;

   // private SourcePointer sourcePointer;
}
