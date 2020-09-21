package org.mvss.karta.samples.tests;

import org.mvss.karta.framework.core.JavaTestCase;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.samples.stepdefinitions.SamplePropertyType;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Test1 implements JavaTestCase
{
   @PropertyMapping( group = "groupName", propertyName = "variable1" )
   private String             username = "default";

   @PropertyMapping( group = "groupName", propertyName = "variable2" )
   private SamplePropertyType variable2;

   @Override
   public void runIteration( TestExecutionContext testExecutionContext )
   {
      log.info( "Test iteration " + username + " " + variable2 );
   }

}
