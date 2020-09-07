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
public class TestExecutionStepPointer implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            testReference;
   private String            scenarioReference;
   private String            stepReference;

   private long              iterationIndex;
   private long              stepIndex;

   // private SourcePointer sourcePointer;
}
