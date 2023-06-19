package org.mvss.karta.server.config;

import org.mvss.karta.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Component
@EnableSwagger2
public class SwaggerConfiguration {
    @Bean
    public Docket swaggerDocket() {
        ApiInfo apiInfo = new ApiInfo("Karta", "Karta Server", "0.1.1", "BSD-3-Clause License",
                new Contact("Manian VSS", "https://github.com/ManianVSS/Karta", "manianvss@hotmail.com"), "API License",
                "https://github.com/ManianVSS/Karta/blob/master/LICENSE", Collections.emptyList());
        return new Docket(DocumentationType.SWAGGER_2).select()
                .paths(PathSelectors.ant(Constants.ANT_MATCHER_API).or(PathSelectors.ant(Constants.ANT_MATCHER_RUN)))
                .apis(RequestHandlerSelectors.basePackage("org.mvss.karta.server")).build().apiInfo(apiInfo);
    }
}
