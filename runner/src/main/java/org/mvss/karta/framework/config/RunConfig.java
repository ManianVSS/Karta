package org.mvss.karta.framework.config;

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
   private String name;
   private String author;
   private String className;
}
