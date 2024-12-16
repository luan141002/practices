package com.personal.practices.config;

import com.personal.practices.config.security.ServiceConfig;
import com.personal.practices.service.machine_id_handler.DefaultMachineIdResolver;
import com.personal.practices.service.machine_id_handler.MachineIdResolver;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Map;

@Configuration
@EnableAsync
public class AppConfig implements AsyncConfigurer {
    @Bean("serviceConfig")
    @ConfigurationProperties("snowflake")
    public ServiceConfig serviceConfig() {
        return new ServiceConfig();
    }

    @Bean("handlerRegister")
    public Map<Class<?>, MachineIdResolver> handlerRegister() {
        return Map.of(
                Integer.class, new DefaultMachineIdResolver()
        );
    }
}
