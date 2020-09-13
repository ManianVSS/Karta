package org.mvss.karta.framework.runtime.models;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.enums.ExecutionStatus;

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
public class IterationExecutionRecord implements Serializable
{

   /**
    * 
    */
   private static final long             serialVersionUID = 1L;

   private ExecutionStatus               executionStatus  = ExecutionStatus.SCHEDULED;

   private HashMap<String, Serializable> testData;

   private HashMap<String, Serializable> runtimeVariables;

}
