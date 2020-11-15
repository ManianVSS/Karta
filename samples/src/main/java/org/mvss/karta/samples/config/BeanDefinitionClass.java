package org.mvss.karta.samples.config;

import org.mvss.karta.framework.core.KartaBean;
import org.mvss.karta.samples.resources.AutomationDriver;
import org.mvss.karta.samples.resources.AutomationDriverImpl;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BeanDefinitionClass
{
   @KartaBean( "AutomationDriverObject" )
   public static AutomationDriver getAutomtionDriver()
   {
      log.info( "Creating new Automation Driver" );
      return AutomationDriverImpl.builder().url( "url" ).username( "user" ).password( "pass" ).build();
   }
}
