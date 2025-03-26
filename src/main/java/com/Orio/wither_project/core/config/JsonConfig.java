package com.Orio.wither_project.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class JsonConfig {

    @Bean
    ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }
}
