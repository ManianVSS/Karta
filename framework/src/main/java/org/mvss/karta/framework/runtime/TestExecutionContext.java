package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestExecutionContext implements Serializable
{

   /**
    * 
    */
   private static final long                              serialVersionUID = 1L;

   private HashMap<String, HashMap<String, Serializable>> testProperties   = null;
   private HashMap<String, Serializable>                  testData         = null;
   private HashMap<String, Serializable>                  runtimeVariables = new HashMap<String, Serializable>();
}
