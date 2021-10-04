package org.mvss.karta.samples.pom.w3s;

import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.web.PageException;
import org.mvss.karta.framework.web.WebAUT;
import org.mvss.karta.framework.web.WebDriverOptions;

public class W3SchoolsApp extends WebAUT
{
   public static final String W_3_SCHOOLS_APP = "W3SchoolsApp";

   @PropertyMapping( group = "WebAutomation", value = "w3SchoolsURL" )
   protected String w3SchoolsURL = "https://www.w3schools.com/";

   public W3SchoolsApp( KartaRuntime kartaRuntime, WebDriverOptions webDriverOptions )
   {
      super( kartaRuntime, "W3 Schools App", webDriverOptions );
   }

   @Override
   public void openStartPage() throws PageException
   {
      driver.navigateTo( w3SchoolsURL );
      new HomePage( this );
   }
}
