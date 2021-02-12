package org.mvss.karta.samples.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutomationDriverImpl implements AutomationDriver
{
   private Browser browser;
   private String  url;

   @Override
   public void click( String locator )
   {
      log.info( "Clicking on element with locator " + locator + " using driver " + this );
   }

   @Override
   public void close() throws Exception
   {
      log.info( "Closing driver " + this );
   }
}
