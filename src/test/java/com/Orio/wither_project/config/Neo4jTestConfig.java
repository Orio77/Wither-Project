package com.Orio.wither_project.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.ai.vectorstore.Neo4jVectorStore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Profile("test")
@EnableAutoConfiguration(exclude = { Neo4jAutoConfiguration.class })
public class Neo4jTestConfig {

    private Neo4jContainer<?> neo4jContainer;

    @SuppressWarnings("resource")
    @PostConstruct
    void startContainer() {
        log.info("Starting Neo4j test container");
        neo4jContainer = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.15"))
                .withoutAuthentication(); // For simplicity in tests
        neo4jContainer.start();

        // Output the container info for debugging
        log.info("Neo4j container started on bolt://{}:{}",
                neo4jContainer.getHost(),
                neo4jContainer.getMappedPort(7687));

        // Set system property to be used in application-test.yml
        System.setProperty("spring.neo4j.uri",
                String.format("neo4j://%s:%d",
                        neo4jContainer.getHost(),
                        neo4jContainer.getMappedPort(7687)));
    }

    @PreDestroy
    void stopContainer() {
        if (neo4jContainer != null) {
            log.info("Stopping Neo4j test container");
            neo4jContainer.stop();
        }
    }

    @Bean(destroyMethod = "close")
    @Primary
    Driver testNeo4jDriver() {
        log.info("Creating test Neo4j driver");
        return GraphDatabase.driver(
                System.getProperty("spring.neo4j.uri"),
                AuthTokens.none());
    }

    @Bean
    @Primary
    Neo4jVectorStore.Neo4jVectorStoreConfig testVectorStoreConfig() {
        return Neo4jVectorStore.Neo4jVectorStoreConfig.builder()
                .withDatabaseName("neo4j") // Default name for test
                .withEmbeddingDimension(768)
                .build();
    }
}