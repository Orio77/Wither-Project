package com.Orio.wither_project.config;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class EmbeddingModelConfig {
    private final OllamaApi ollamaApi;

    @Bean
    @Primary
    EmbeddingClient embeddingModel() {
        OllamaOptions options = new OllamaOptions();
        options.setModel("nomic-embed-text:latest");
        return new OllamaEmbeddingClient(ollamaApi).withDefaultOptions(options);
    }
}
