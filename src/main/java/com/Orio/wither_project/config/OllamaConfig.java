package com.Orio.wither_project.config;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
public class OllamaConfig {

    @Bean
    public OllamaApi getOllamaApi() {
        return new OllamaApi();
    }

    @Setter
    @Value("${ollama.text.model}")
    private String model;

    @Value("${ollama.text.numCTX}")
    private int numCTX;

    @Value("${ollama.text.temperature}")
    private float temperature;
}
