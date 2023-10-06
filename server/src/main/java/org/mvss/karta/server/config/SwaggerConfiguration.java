package org.mvss.karta.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SwaggerConfiguration {
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Karta")
                        .description("Karta Server")
                        .contact(new Contact().name("Manian VSS").email("manianvss@hotmail.com").url("https://github.com/ManianVSS/Karta"))
                        .version("0.1.1")
                        .license(new License().name("BSD-3-Clause").url("https://github.com/ManianVSS/Karta/blob/master/LICENSE")));
    }
}
