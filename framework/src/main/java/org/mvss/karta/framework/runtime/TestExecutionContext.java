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

   private String                                         runName;
   private HashMap<String, HashMap<String, Serializable>> properties       = null;
   private HashMap<String, Serializable>                  data             = null;
   private HashMap<String, Serializable>                  variables        = new HashMap<String, Serializable>();
}
