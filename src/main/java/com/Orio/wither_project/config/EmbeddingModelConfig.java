package com.Orio.wither_project.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EmbeddingModelConfig {
    private final OllamaApi ollamaApi;

    @Bean
    @Primary
    EmbeddingModel embeddingModel() {
        OllamaOptions options = new OllamaOptions();
        options.setModel("nomic-embed-text:latest"); // TODO extract to constants

        ModelManagementOptions modelManagementOptions = ModelManagementOptions.builder().build();
        ObservationRegistry observationRegistry = ObservationRegistry.create();

        return new OllamaEmbeddingModel(ollamaApi, options, observationRegistry, modelManagementOptions); // TODO Check
                                                                                                          // if it's
                                                                                                          // working
    }
}
