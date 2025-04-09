package com.Orio.wither_project.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.Neo4jException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.config.Neo4jTestConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = { Neo4jTestConfig.class })
public class Neo4jTest {

    @Autowired
    private Driver neo4jDriver;

    private final AtomicBoolean databaseAvailable = new AtomicBoolean(false);

    @BeforeEach
    void checkDatabaseAvailability() {
        try (Session session = neo4jDriver.session(org.neo4j.driver.SessionConfig.builder()
                .withDefaultAccessMode(org.neo4j.driver.AccessMode.READ)
                .build())) {
            Result result = session.run("RETURN 1");
            result.consume();
            databaseAvailable.set(true);
            log.info("Neo4j database is available");
        } catch (Neo4jException e) {
            databaseAvailable.set(false);
            log.warn("Neo4j database is not available: {}", e.getMessage());
        }
    }

    @Test
    @DisplayName("Neo4j Test Connection")
    void shouldConnectToTestNeo4j() {
        assumeTrue(databaseAvailable.get(), "Database is not available");
        log.info("Testing Neo4j connection");

        try (Session session = neo4jDriver.session()) {
            Result result = session.run("RETURN 1 AS n");
            int value = result.single().get("n").asInt();
            assertEquals(1, value, "Should be able to execute a simple Cypher query");
            log.info("Successfully connected to Neo4j test instance");
        }
    }

    @Test
    @DisplayName("Database Should Be Empty")
    void shouldHaveEmptyDatabase() {
        log.info("Verifying test database is empty");

        try (Session session = neo4jDriver.session()) {
            // Count all nodes in the database
            Result result = session.run("MATCH (n) RETURN count(n) as nodeCount");
            long nodeCount = result.single().get("nodeCount").asLong();

            // Assert that there are no nodes in the database
            assertEquals(0, nodeCount, "Test database should be empty with no nodes");
            log.info("Confirmed database is empty with 0 nodes");

            // Verify no relationships exist
            Result relationshipResult = session.run("MATCH ()-[r]->() RETURN count(r) as relCount");
            long relCount = relationshipResult.single().get("relCount").asLong();
            assertEquals(0, relCount, "Test database should have no relationships");
            log.info("Confirmed database has 0 relationships");
        }
    }

    @Test
    @DisplayName("Can Create and Query Data")
    void shouldCreateAndQueryData() {
        log.info("Testing Neo4j data operations");

        // Create test data
        try (Session session = neo4jDriver.session()) {
            session.run("CREATE (n:TestNode {name: 'test'}) RETURN n");
        }

        // Query and verify
        try (Session session = neo4jDriver.session()) {
            Result result = session.run("MATCH (n:TestNode) RETURN n.name as name");
            assertTrue(result.hasNext(), "Should have created a node");
            String name = result.single().get("name").asString();
            assertEquals("test", name, "Node should have correct property");
        }

        // Clean up
        try (Session session = neo4jDriver.session()) {
            session.run("MATCH (n:TestNode) DELETE n");
        }
    }
}