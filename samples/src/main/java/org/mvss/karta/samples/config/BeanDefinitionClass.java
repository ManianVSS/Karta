package org.mvss.karta.samples.config;

import org.mvss.karta.framework.core.KartaAutoWired;
import org.mvss.karta.framework.core.KartaBean;
import org.mvss.karta.framework.runtime.Configurator;
import org.mvss.karta.samples.stepdefinitions.Employee;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class BeanDefinitionClass
{
   @KartaAutoWired
   private static Configurator configurator;

   @KartaBean( "EmployeeBean" )
   public static Employee getEmployee()
   {
      if ( configurator == null )
      {
         log.error( "Configurator not initialized." );
      }

      log.info( "Creating new bean" );
      return new Employee( "AdminBeanEmployee", "admin", "NA", false, "admin", 0, "NA" );
   }
}
