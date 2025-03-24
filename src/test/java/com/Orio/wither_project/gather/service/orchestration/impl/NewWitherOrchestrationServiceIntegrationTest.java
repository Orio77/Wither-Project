package com.Orio.wither_project.gather.service.orchestration.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.exception.InvalidQueryException;
import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.service.format.IFormatService;
import com.Orio.wither_project.gather.service.scrape.IScrapeService;
import com.Orio.wither_project.gather.service.search.ISearchService;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class NewWitherOrchestrationServiceIntegrationTest {

    @Autowired
    private ISearchService searchService;

    @Autowired
    private IFormatService formatService;

    @Autowired
    private IScrapeService scrapeService;

    private NewWitherOrchestrationService orchestrationService;

    @BeforeEach
    void setUp() {
        orchestrationService = new NewWitherOrchestrationService(searchService, formatService, scrapeService);
    }

    @Test
    @DisplayName("Should gather data when valid query is provided")
    void testGatherData_WithValidQuery() {
        // Given
        String query = "Ancient Sparta";

        try {
            // When
            DataModel result = orchestrationService.gatherData(query);

            // Then
            assertNotNull(result, "The returned DataModel should not be null");
            log.info("Successfully gathered data for query: {}", query);
            log.info("Result: {}", result);
        } catch (Exception e) {
            log.error("Error during data gathering: ", e);
            throw e;
        }
    }

    @Test
    @DisplayName("Should throw exception when invalid query is provided")
    void testGatherData_WithInvalidQuery() {
        // Given
        String query = ""; // Empty query should fail

        // When & Then
        Exception exception = assertThrows(InvalidQueryException.class, () -> {
            orchestrationService.gatherData(query);
        });

        log.info("Expected exception thrown: {}", exception.getMessage());
    }
}
