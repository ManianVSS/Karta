package org.mvss.karta.server.beans;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.mvss.karta.framework.utils.NullAwareBeanUtilsBean;
import org.mvss.karta.framework.utils.ParserUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilBeans {
    @Bean
    public ObjectMapper getObjectMapper() {
        return ParserUtils.getObjectMapper();
    }

    @Bean
    public BeanUtilsBean getBeanUtilsBean() {
        return new NullAwareBeanUtilsBean();
    }
}
