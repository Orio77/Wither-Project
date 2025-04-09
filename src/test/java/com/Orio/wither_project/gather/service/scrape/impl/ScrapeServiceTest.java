package com.Orio.wither_project.gather.service.scrape.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.gather.config.ScrapeLinkConfig;
import com.Orio.wither_project.gather.model.DataSource;
import com.Orio.wither_project.gather.model.ScrapeResult;
import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.model.SearchResult.SearchItem;
import com.Orio.wither_project.gather.service.scrape.IScrapeService;

@SpringBootTest
@ActiveProfiles("test")
class ScrapeServiceTest {

    @Autowired
    private ScrapeLinkConfig linkConfig;

    @Autowired
    private IScrapeService scrapeService;

    @Test
    void testScrapeWithEmptyDataSource() {
        // Arrange
        DataSource dataSource = DataSource.builder()
                .query("test query")
                .items(new ArrayList<>())
                .build();

        // Act
        ScrapeResult result = scrapeService.scrape(dataSource);

        // Assert
        assertNotNull(result);
        assertEquals("test query", result.getQuery());
        assertTrue(result.getItems().isEmpty());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testScrapeWithNullItems() {
        // Arrange
        DataSource dataSource = DataSource.builder()
                .query("test query")
                .items(null)
                .build();

        // Act
        ScrapeResult result = scrapeService.scrape(dataSource);

        // Assert
        assertNotNull(result);
        assertEquals("test query", result.getQuery());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testScrapeWithConfiguredLinks() {
        // Skip test if no URLs configured
        assertNotNull(linkConfig.getTestUrls(), "Test URLs should be configured");
        assertNotEquals(0, linkConfig.getTestUrls().size(), "Test URLs list should not be empty");

        // Arrange
        List<SearchItem> items = new ArrayList<>();

        // Create items from configured test URLs
        for (String url : linkConfig.getTestUrls()) {
            SearchItem item = SearchItem.builder()
                    .link(url)
                    .build();
            items.add(item);
        }

        DataSource dataSource = DataSource.builder()
                .query("test query")
                .items(items)
                .build();

        // Act
        ScrapeResult result = scrapeService.scrape(dataSource);

        // Assert
        assertNotNull(result);
        assertFalse(result.getItems().isEmpty(), "At least some URLs should be successfully scraped");

        // Check that each scraped item has content
        for (ScrapeItem item : result.getItems()) {
            assertNotNull(item.getContent(), "Content should be extracted");
            assertFalse(item.getContent().isEmpty(), "Content should not be empty");
            assertNotNull(item.getTitle(), "Title should be extracted");
        }
    }

    @Test
    void testScrapeWithInvalidUrls() {
        // Arrange
        List<SearchItem> items = new ArrayList<>();

        // Add some invalid URLs
        SearchItem item1 = SearchItem.builder()
                .link("http://this-is-an-invalid-url-that-should-not-exist.com/test")
                .build();
        items.add(item1);

        SearchItem item2 = SearchItem.builder()
                .link("not-even-a-valid-url")
                .build();
        items.add(item2);

        DataSource dataSource = DataSource.builder()
                .query("invalid URLs test")
                .items(items)
                .build();

        // Act
        ScrapeResult result = scrapeService.scrape(dataSource);

        // Assert
        assertNotNull(result);
        assertTrue(result.getItems().isEmpty(), "No items should be successfully scraped");
        assertFalse(result.getErrors().isEmpty(), "Errors should be recorded");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testScrapeWithValidUrl() {
        // Replace this URL with a reliable one for your tests
        String validUrl = "https://en.wikipedia.org/wiki/Thought";

        // Act
        ScrapeResult result = scrapeService.scrape(validUrl);

        // Assert
        assertNotNull(result);
        assertEquals(validUrl, result.getQuery());
        assertFalse(result.getItems().isEmpty(), "Should have at least one item");
        assertTrue(result.getErrors().isEmpty(), "Should have no errors");

        // Verify the content of the scraped item
        ScrapeItem scrapeItem = result.getItems().get(0);
        assertNotNull(scrapeItem.getContent(), "Content should be extracted");
        assertFalse(scrapeItem.getContent().isEmpty(), "Content should not be empty");
        assertNotNull(scrapeItem.getTitle(), "Title should be extracted");
        assertEquals(validUrl, scrapeItem.getLink(), "Link should match the input URL");
    }

    @Test
    void testScrapeWithInvalidUrl() {
        // Arrange - use an invalid URL that shouldn't exist
        String invalidUrl = "http://this-is-an-invalid-url-that-should-not-exist-12345.com/test";

        // Act
        ScrapeResult result = scrapeService.scrape(invalidUrl);

        // Assert
        assertNotNull(result);
        assertEquals(invalidUrl, result.getQuery());
        assertTrue(result.getItems().isEmpty(), "Should have no items");
        assertFalse(result.getErrors().isEmpty(), "Should have at least one error");
    }

    @Test
    void testScrapeWithNullOrEmptyUrl() {
        // Act with null URL
        ScrapeResult nullResult = scrapeService.scrape((String) null);

        // Assert
        assertNotNull(nullResult);
        assertTrue(nullResult.getItems().isEmpty(), "Should have no items");
        assertFalse(nullResult.getErrors().isEmpty(), "Should have at least one error");

        // Act with empty URL
        ScrapeResult emptyResult = scrapeService.scrape("");

        // Assert
        assertNotNull(emptyResult);
        assertTrue(emptyResult.getItems().isEmpty(), "Should have no items");
        assertFalse(emptyResult.getErrors().isEmpty(), "Should have at least one error");
    }
}
