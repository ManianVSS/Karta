package org.mvss.karta.framework.runtime.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class RunStartEvent extends Event
{
   private static final long serialVersionUID = 1L;

   public RunStartEvent( Event event )
   {
      super( event );
   }

   public RunStartEvent( String runName )
   {
      super( StandardEventsTypes.RUN_START_EVENT, runName );
   }
}
