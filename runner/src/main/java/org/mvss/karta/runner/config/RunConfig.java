package org.mvss.karta.runner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties( prefix = "run" )
@PropertySource( "classpath:run.properties" )
public class RunConfig
{
   private String className;
   private String jarFile;
}
