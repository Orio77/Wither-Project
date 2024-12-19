package com.Orio.wither_project.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.ai.vectorstore.Neo4jVectorStore.Neo4jVectorStoreConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {

    @Bean
    Driver driver() {
        return GraphDatabase.driver("bolt://localhost:7687",
                AuthTokens.basic("neo4j", "12345678"));
    }

    @Bean
    Neo4jVectorStoreConfig config() {
        return Neo4jVectorStoreConfig.builder().withDatabaseName("Web Scraping Vector Store")
                .withEmbeddingDimension(768).build();
    }

    // @Bean
    // public Neo4jVectorStoreCopy.Neo4jVectorStoreConfig neo4jVectorStoreConfig() {
    // return Neo4jVectorStoreCopy.Neo4jVectorStoreConfig.defaultConfig();
    // }
}
