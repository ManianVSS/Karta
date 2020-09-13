package org.mvss.karta.server.beans;

import org.mvss.karta.framework.runtime.FeatureRunner;
import org.mvss.karta.framework.runtime.JavaTestRunner;
import org.mvss.karta.framework.runtime.RuntimeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RunnerBeans
{
   @Autowired
   private ObjectMapper     objectMapper;

   @Autowired
   private RuntimeConfiguration runtimeConfiguration;

   @Bean
   public JavaTestRunner getJavaTestRunner()
   {
      JavaTestRunner testRunner = objectMapper.convertValue( runtimeConfiguration, JavaTestRunner.class );
      return testRunner;
   }

   @Bean
   public FeatureRunner getFeatureRunner()
   {
      FeatureRunner featureRunner = objectMapper.convertValue( runtimeConfiguration, FeatureRunner.class );
      return featureRunner;
   }
}
