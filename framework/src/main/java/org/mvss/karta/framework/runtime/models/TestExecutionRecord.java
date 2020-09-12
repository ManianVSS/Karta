package org.mvss.karta.framework.runtime.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

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
public class TestExecutionRecord implements Serializable
{

   /**
    * 
    */
   private static final long                   serialVersionUID          = 1L;

   private HashMap<String, Serializable>       testProperties;

   private ArrayList<IterationExecutionRecord> iterationExecutionRecords = new ArrayList<IterationExecutionRecord>();
}
