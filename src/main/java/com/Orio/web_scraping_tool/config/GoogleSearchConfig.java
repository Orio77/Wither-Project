package com.Orio.web_scraping_tool.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class GoogleSearchConfig {

    @Value("${google.api.key}")
    private String apiKey;
    @Value("${google.search.engine.id}")
    private String searchEngineId;
    @Value("${google.search.url}")
    private String searchURL;
    @Value("${google.search.numResults}")
    private int numResults;
}
