package com.Orio.web_scraping_tool.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EmbeddingModelConfig {
    private final OllamaApi ollamaApi;

    @Bean
    public EmbeddingModel embeddingModel() {
        OllamaOptions options = new OllamaOptions();
        options.withModel("nomic-embed-text:latest");
        return new OllamaEmbeddingModel(ollamaApi, options, ObservationRegistry.create(),
                ModelManagementOptions.defaults());
    }
}
