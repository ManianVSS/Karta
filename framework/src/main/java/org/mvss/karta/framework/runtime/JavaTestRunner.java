package org.mvss.karta.framework.runtime;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.JavaTestCase;
import org.mvss.karta.framework.utils.DynamicClassLoader;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@NoArgsConstructor
@Log4j2
@JsonInclude( value = Include.NON_ABSENT, content = Include.NON_ABSENT )
@Builder
public class JavaTestRunner
{
   public static final String RUN_PROPERTIES_FILE = "Run.properties";

   @SuppressWarnings( "unchecked" )
   public boolean run( String javaTest, String javaTestJarFile )
   {
      try
      {
         Class<? extends JavaTestCase> testCaseClass = StringUtils.isNotBlank( javaTestJarFile ) ? (Class<? extends JavaTestCase>) DynamicClassLoader.LoadClass( javaTestJarFile, javaTest ) : (Class<? extends JavaTestCase>) Class.forName( javaTest );
         JavaTestCase testCase = testCaseClass.newInstance();

         testCase.beforeTest();
         testCase.runTest();
         testCase.afterTest();
      }
      catch ( ClassNotFoundException cnfe )
      {
         log.error( "class " + javaTest + " could not be loaded" );
         return false;
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured when trying to run test", t );
         return false;
      }
      return true;
   }
}
