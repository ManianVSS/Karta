package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
   private Date                          startTime        = new Date();

   private Date                          endTime;

   @Builder.Default
   private boolean                       successsful      = true;

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

   public void merge( StepResult stepResult )
   {
      if ( stepResult == null )
      {
         return;
      }

      if ( stepResult.startTime != null )
      {
         this.startTime = stepResult.startTime;
      }

      if ( stepResult.endTime != null )
      {
         this.endTime = stepResult.endTime;
      }

      successsful = successsful && stepResult.successsful;
      error = error && stepResult.error;

      if ( stepResult.incidents != null )
      {
         if ( incidents == null )
         {
            incidents = new ArrayList<TestIncident>();
         }
         incidents.addAll( stepResult.incidents );
      }

      if ( stepResult.results != null )
      {
         if ( results == null )
         {
            results = new HashMap<String, Serializable>();
         }
         this.results.putAll( stepResult.results );
      }

      if ( stepResult.events != null )
      {
         if ( events == null )
         {
            events = new ArrayList<Event>();
         }
         events.addAll( stepResult.events );
      }

   }
}
