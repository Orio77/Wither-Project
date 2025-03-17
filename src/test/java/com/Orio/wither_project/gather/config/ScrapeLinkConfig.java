package com.Orio.wither_project.gather.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for scrape links used in tests and production
 */
@Configuration
@ConfigurationProperties(prefix = "scrape.links")
@Getter
@Setter
public class ScrapeLinkConfig {

    /**
     * URLs used for testing the scraper
     */
    private List<String> testUrls;

    /**
     * URLs used in production environment
     */
    private List<String> productionUrls;
}
