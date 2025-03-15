package com.Orio.wither_project.gather.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Builder;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "scrape")
@Data
public class ScrapeConfig {
        /**
         * Connection timeout in seconds
         */
        private final int timeoutSeconds = 10;

        /**
         * Maximum number of threads to use for scraping TODO extract to properties
         */
        private final int threadPoolSize = 10;

        /**
         * Maximum number of retry attempts for failed scrapes
         */
        private final int maxRetries = 3;

        /**
         * User agent string to use for HTTP requests
         */
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        /**
         * Content extraction selectors
         */
        private ContentSelectors contentSelectors = ContentSelectors.builder().build();

        @Data
        @Builder
        public static class ContentSelectors {
                /**
                 * CSS selectors for content containers, in order of preference
                 */
                @Builder.Default
                private String[] contentContainers = {
                                "article", "main", "#content", ".content", ".post-content",
                                ".entry-content", ".article-body", "[itemprop=articleBody]",
                                ".post", ".entry", ".blog-post"
                };

                /**
                 * CSS selectors for author information, in order of preference
                 */
                @Builder.Default
                private String[] authorSelectors = {
                                "[rel=author]", ".author", ".byline", "[itemprop=author]",
                                ".post-author", ".entry-author", "meta[name=author]"
                };

                /**
                 * CSS selectors for publication date, in order of preference
                 */
                @Builder.Default
                private String[] dateSelectors = {
                                "[itemprop=datePublished]", "time", ".date", ".published",
                                ".post-date", ".entry-date", "meta[property=article:published_time]"
                };
        }
}