package org.mvss.karta.framework.core;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.utils.ExtensionLoader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@JsonInclude( value = Include.NON_ABSENT, content = Include.NON_ABSENT )
@Builder
public class TestRunner implements Runnable
{
   public static final String                     RUN_PROPERTIES_FILE = "Run.properties";

   private static final ExtensionLoader<TestCase> testCaseLoader      = new ExtensionLoader<TestCase>();

   private String                                 className;
   private String                                 jarFile;

   @SuppressWarnings( "unchecked" )
   @Override
   public void run()
   {
      try
      {
         Class<? extends TestCase> testCaseClass = StringUtils.isNotBlank( jarFile ) ? testCaseLoader.LoadClass( new File( jarFile ), className ) : (Class<? extends TestCase>) Class.forName( className );
         TestCase testCase = testCaseClass.newInstance();

         testCase.beforeTest();
         testCase.runTest();
         testCase.afterTest();
      }
      catch ( ClassNotFoundException cnfe )
      {
         log.error( "class " + className + " could not be loaded" );
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured when trying to run test", t );
      }
   }
}
