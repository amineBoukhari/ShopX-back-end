package com.olatech.shopxauthservice.config;

import com.olatech.shopxauthservice.Service.LocalFileStorageService;
import com.olatech.shopxauthservice.Service.LocalFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class StorageConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
    public LocalFileStorageService localFileStorageService() {
        log.info("Configuring Local File Storage Service");
        return new LocalFileStorageService();
    }
}