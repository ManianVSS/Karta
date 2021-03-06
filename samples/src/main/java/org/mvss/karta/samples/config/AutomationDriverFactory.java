package org.mvss.karta.samples.config;

import org.mvss.karta.framework.core.LoadConfiguration;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.samples.resources.AutomationDriver;
import org.mvss.karta.samples.resources.AutomationDriverImpl;
import org.mvss.karta.samples.resources.Browser;

@LoadConfiguration
public class AutomationDriverFactory
{
   @PropertyMapping( group = "AutomationDriver", value = "browser" )
   private static Browser browser;

   @PropertyMapping( group = "AutomationDriver", value = "url" )
   private static String  url;

   public static AutomationDriver createAutomationDriver()
   {
      return new AutomationDriverImpl( browser, url );
   }
}
