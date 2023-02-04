package org.mvss.karta.server.beans;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class ConfigBeans {
    @Bean
    public KartaRuntime getKartaRuntime() {
        try {
            KartaRuntime.initializeNodes = false;
            KartaRuntime kartaRuntime = KartaRuntime.getInstance();

            if (kartaRuntime == null) {
                log.error("Karta runtime could not be initialized. Please check the directory and config files");
                System.exit(-1);
            }

            return kartaRuntime;
        } catch (Throwable e) {
            log.error(e);
            System.exit(1);
        }
        return null;
    }
}
