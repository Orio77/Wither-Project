package com.Orio.wither_project.config;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@Getter
public class OllamaConfig {

    @Bean
    OllamaApi getOllamaApi() {
        return new OllamaApi();
    }

    @Bean
    OllamaOptions getOllamaOptions() {
        return OllamaOptions.builder().withModel(this.model).withNumCtx(this.numCTX)
                .withTemperature(this.temperature)
                .build();
    }

    @Bean
    OllamaChatModel getOllamaChatModel() {
        return OllamaChatModel.builder().withOllamaApi(getOllamaApi()).withDefaultOptions(getOllamaOptions()).build();
    }

    public OllamaChatModel getOllamaChatModelHighNumCTX() {
        return OllamaChatModel.builder().withOllamaApi(getOllamaApi())
                .withDefaultOptions(getOllamaOptions().withNumCtx(numCTXHigh)).build();
    }

    public OllamaChatModel getOllamaChatModelLowNumCTX() {
        return OllamaChatModel.builder().withOllamaApi(getOllamaApi())
                .withDefaultOptions(getOllamaOptions().withNumCtx(numCTXLow)).build();
    }

    @Setter
    @Value("${ollama.text.model}")
    private String model;

    @Value("${ollama.text.numCTX.default}")
    private int numCTX;

    @Value("${ollama.text.numCTX.low}")
    private int numCTXLow;

    @Value("${ollama.text.numCTX.high}")
    private int numCTXHigh;

    @Value("${ollama.text.temperature}")
    private double temperature;
}
