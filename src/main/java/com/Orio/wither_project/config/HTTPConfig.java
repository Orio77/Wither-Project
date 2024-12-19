package com.Orio.wither_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HTTPConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
