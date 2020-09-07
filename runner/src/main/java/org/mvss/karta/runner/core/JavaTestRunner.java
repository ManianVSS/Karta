package org.mvss.karta.runner.core;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.JavaTestCase;
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
public class JavaTestRunner implements Runnable
{
   public static final String                         RUN_PROPERTIES_FILE = "Run.properties";

   private static final ExtensionLoader<JavaTestCase> testCaseLoader      = new ExtensionLoader<JavaTestCase>();

   private String                                     className;
   private String                                     jarFile;

   @SuppressWarnings( "unchecked" )
   @Override
   public void run()
   {
      try
      {
         Class<? extends JavaTestCase> testCaseClass = StringUtils.isNotBlank( jarFile ) ? testCaseLoader.LoadClass( new File( jarFile ), className ) : (Class<? extends JavaTestCase>) Class.forName( className );
         JavaTestCase testCase = testCaseClass.newInstance();

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
