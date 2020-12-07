package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestJobResult implements Serializable, Comparable<TestJobResult>
{
   /**
    * 
    */
   private static final long                           serialVersionUID = 1L;

   @Builder.Default
   private long                                        iterationIndex   = 0;

   @Builder.Default
   private Date                                        startTime        = new Date();

   private Date                                        endTime;

   @Builder.Default
   private boolean                                     successsful      = true;

   @Builder.Default
   private boolean                                     error            = false;

   @Builder.Default
   private ArrayList<SerializableKVP<String, Boolean>> stepResults      = new ArrayList<SerializableKVP<String, Boolean>>();

   @Override
   public int compareTo( TestJobResult other )
   {
      return ( iterationIndex < other.iterationIndex ) ? -1 : ( ( iterationIndex == other.iterationIndex ) ? 0 : 1 );
   }
}
