package org.mvss.karta.samples.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutomationDriverImpl implements AutomationDriver
{
   private String url;
   private String username;
   private String password;

   @Override
   public void click( String locator )
   {
      log.info( "Clicking on element with locator " + locator + " using driver " + this );
   }
}
