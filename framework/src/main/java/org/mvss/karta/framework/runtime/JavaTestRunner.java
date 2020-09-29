package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.JavaTestCase;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.utils.DynamicClassLoader;

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
public class JavaTestRunner
{
   @Builder.Default
   @PropertyMapping( propertyName = "RunProperties" )
   private RunProperties                                  runProperties = new RunProperties();

   private HashMap<String, HashMap<String, Serializable>> testProperties;

   @SuppressWarnings( "unchecked" )
   public boolean run( String javaTest, String javaTestJarFile )
   {
      try
      {
         Class<? extends JavaTestCase> testCaseClass = StringUtils.isNotBlank( javaTestJarFile ) ? (Class<? extends JavaTestCase>) DynamicClassLoader.loadClass( javaTestJarFile, javaTest ) : (Class<? extends JavaTestCase>) Class.forName( javaTest );
         JavaTestCase testCase = testCaseClass.newInstance();
         Configurator.loadProperties( testProperties, testCase );

         HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         TestExecutionContext testExecutionContext = new TestExecutionContext( testProperties, testData, variables );

         testCase.beforeTest( testExecutionContext );
         testCase.runTest( testExecutionContext );
         testCase.afterTest( testExecutionContext );
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
