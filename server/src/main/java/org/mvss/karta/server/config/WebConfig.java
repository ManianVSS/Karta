package org.mvss.karta.server.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.server.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${frontend.views:/WebUI/**,/ui/**}")
    private String[] frontendViews;

    @SneakyThrows
    public void addViewControllers(ViewControllerRegistry registry) {
        if (frontendViews != null) {
            for (String frontendView : frontendViews) {
                log.info("Mapping frontend route  " + frontendView + " from properties to " + Constants.DEFAULT_VIEW);
                registry.addViewController(frontendView).setViewName(Constants.SLASH);//Constants.DEFAULT_VIEW );
            }
        }

        File file = new File(Constants.APPLICATION_ROUTE_MAP_YAML);

        if (file.isFile()) {
            HashMap<String, String> applicationRouteMapping = ParserUtils.readValue(DataFormat.YAML,
                    FileUtils.readFileToString(file, Charset.defaultCharset()), ParserUtils.stringHashMapObjectType);
            applicationRouteMapping.forEach((key, value) -> {
                log.info("Mapping frontend view " + key + "from app routes config to " + value);
                registry.addViewController(key).setViewName(value);
            });
        }
    }
}
