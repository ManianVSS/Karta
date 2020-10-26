package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.event.Event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StepResult implements Serializable
{
   /**
    * 
    */
   private static final long             serialVersionUID = 1L;

   @Builder.Default
   private boolean                       successsful      = false;

   @Builder.Default
   private boolean                       error            = false;

   @Builder.Default
   private ArrayList<TestIncident>       incidents        = new ArrayList<TestIncident>();

   @Builder.Default
   private HashMap<String, Serializable> results          = new HashMap<String, Serializable>();

   @Builder.Default
   private ArrayList<Event>              events           = new ArrayList<Event>();

   public void addIncident( TestIncident testIncident )
   {
      if ( incidents == null )
      {
         incidents = new ArrayList<TestIncident>();
      }
      incidents.add( testIncident );
   }
}
