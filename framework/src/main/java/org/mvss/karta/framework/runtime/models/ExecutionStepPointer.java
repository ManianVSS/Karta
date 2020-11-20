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

   private String            feature;
   private String            scenario;
   private String            step;

   private long              iterationIndex;
   private int               stepIndex;

   // private SourcePointer sourcePointer;
}
