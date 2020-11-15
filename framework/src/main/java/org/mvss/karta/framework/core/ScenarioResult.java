package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResult implements Serializable, Comparable<ScenarioResult>
{
   /**
    * 
    */
   private static final long        serialVersionUID   = 1L;

   @Builder.Default
   private int                      iterationIndex     = 0;

   @Builder.Default
   private Date                     startTime          = new Date();

   private Date                     endTime;

   @Builder.Default
   private boolean                  successful         = true;

   @Builder.Default
   private boolean                  error              = false;

   @Builder.Default
   private ArrayList<TestIncident>  incidents          = new ArrayList<TestIncident>();

   @Builder.Default
   private HashMap<String, Boolean> setupResults       = new HashMap<String, Boolean>();

   @Builder.Default
   private HashMap<String, Boolean> chaosActionResults = new HashMap<String, Boolean>();

   @Builder.Default
   private HashMap<String, Boolean> runResults         = new HashMap<String, Boolean>();

   @Builder.Default
   private HashMap<String, Boolean> tearDownResults    = new HashMap<String, Boolean>();

   public boolean isPassed()
   {
      return successful && !error && incidents.isEmpty();
   }

   @Override
   public int compareTo( ScenarioResult other )
   {
      return iterationIndex - other.iterationIndex;
   }
}
