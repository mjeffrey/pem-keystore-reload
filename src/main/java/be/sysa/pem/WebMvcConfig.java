package be.sysa.pem;

import be.sysa.pem.reload.MultiPortReloadableServletFactory;
import be.sysa.pem.reload.ReloadableServletFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory(Environment environment, ServletWebServerFactoryCustomizer customizer) {
//        return ReloadableServletFactory.create(environment);
        return MultiPortReloadableServletFactory.createWithAdditionalPorts(environment, customizer, 8081, 8082, 8083);
    }

}