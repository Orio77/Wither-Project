package com.Orio.wither_project.gader.service.search.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.gader.model.SearchResult;
import com.Orio.wither_project.gader.service.search.ISearchService;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@DisplayName("Google Search Service Tests")
class SearchServiceTest {

    @Autowired
    private ISearchService searchService;

    @Test
    @DisplayName("Search should return valid results for common query")
    void searchShouldReturnResults() {
        // Given
        String query = "Java programming language";
        log.info("Executing search test with query: '{}'", query);

        // When
        SearchResult result = searchService.search(query);

        // Then
        assertNotNull(result, "Search result should not be null");
        assertEquals(query, result.getQuery(), "Query should match the input");

        log.info("Search returned {} results with {} errors",
                result.getItems() != null ? result.getItems().size() : 0,
                result.getErrors() != null ? result.getErrors().size() : 0);

        assertTrue(result.getItems().size() > 0, "Should return at least one search result");
        if (result.getErrors() != null) {
            assertTrue(result.getErrors().isEmpty(), "Should not contain any errors");
        }

        // Verify first item has title and link
        SearchResult.Item firstItem = result.getItems().get(0);
        assertNotNull(firstItem.getTitle(), "Title should not be null");
        assertNotNull(firstItem.getLink(), "Link should not be null");
        assertFalse(firstItem.getTitle().isEmpty(), "Title should not be empty");
        assertFalse(firstItem.getLink().isEmpty(), "Link should not be empty");

        log.info("First search result: Title='{}', Link='{}'",
                firstItem.getTitle(), firstItem.getLink());
    }

    @Test
    @DisplayName("Search with uncommon query should return few or no results")
    void searchWithUncommonQueryShouldReturnFewOrNoResults() {
        // Given - extremely specific and unlikely search term
        String uncommonQuery = "xyzabcdefg123456789unusualsearchterm";
        log.info("Executing uncommon search test with query: '{}'", uncommonQuery);

        // When
        SearchResult result = searchService.search(uncommonQuery);

        // Then
        assertNotNull(result, "Search result should not be null");
        assertEquals(uncommonQuery, result.getQuery(), "Query should match the input");

        log.info("Uncommon search returned {} results with {} errors",
                result.getItems() != null ? result.getItems().size() : 0,
                result.getErrors() != null ? result.getErrors().size() : 0);

        // Either will have no results or very few
        if (result.getErrors() == null || result.getErrors().isEmpty()) {
            // No errors means we got at least some response, even if empty
            assertNotNull(result.getItems(), "Items should not be null");
            log.info("Uncommon search found {} results", result.getItems().size());
        } else {
            // If there are errors, at least one should be NoSearchResultsException
            boolean hasNoResultsException = result.getErrors().stream()
                    .anyMatch(e -> e instanceof com.Orio.wither_project.gader.exception.NoSearchResultsException);
            assertTrue(hasNoResultsException, "Should have NoSearchResultsException");
            log.info("Uncommon search returned expected NoSearchResultsException");
        }
    }
}
