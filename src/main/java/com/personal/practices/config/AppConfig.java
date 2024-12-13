package com.personal.practices.config;

import com.personal.practices.config.security.ServiceConfig;
import com.personal.practices.service.SnowflakeIdGeneratorService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AppConfig implements AsyncConfigurer {
    @Bean("serviceConfig")
    @ConfigurationProperties("snowflake")
    public ServiceConfig serviceConfig() {
        return new ServiceConfig();
    }

    @Bean
    public SnowflakeIdGeneratorService snowflakeIdGeneratorService(ServiceConfig serviceConfig) {
        return new SnowflakeIdGeneratorService(serviceConfig.getMachineId());
    }
}
