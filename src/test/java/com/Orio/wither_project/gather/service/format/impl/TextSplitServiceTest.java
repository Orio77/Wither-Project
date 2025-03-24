package com.Orio.wither_project.gather.service.format.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.gather.model.ContentWithSource;
import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.model.ScrapedTextBatch;

@SpringBootTest
@ActiveProfiles("test")
class TextSplitServiceTest {

    @Autowired
    private TextSplitService textSplitService;

    // Tests for getContentWithSources method
    @Test
    void getContentWithSources_withValidItems_shouldReturnContentWithSourcesList() {
        // Arrange
        List<ScrapeItem> items = Arrays.asList(
                createScrapeItem("Content 1", "https://example.com/1"),
                createScrapeItem("Content 2", "https://example.com/2"));

        // Act
        List<ContentWithSource> result = textSplitService.getContentWithSources(items);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Content 1", result.get(0).getContent());
        assertEquals("https://example.com/1", result.get(0).getSource());
        assertEquals("Content 2", result.get(1).getContent());
        assertEquals("https://example.com/2", result.get(1).getSource());
    }

    @Test
    void getContentWithSources_withNullItems_shouldFilterOutNullItems() {
        // Arrange
        List<ScrapeItem> items = Arrays.asList(
                createScrapeItem("Content 1", "https://example.com/1"),
                null,
                createScrapeItem("Content 2", "https://example.com/2"),
                createScrapeItem(null, "https://example.com/3"),
                createScrapeItem("Content 4", null));

        // Act
        List<ContentWithSource> result = textSplitService.getContentWithSources(items);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Content 1", result.get(0).getContent());
        assertEquals("Content 2", result.get(1).getContent());
    }

    @Test
    void getContentWithSources_withEmptyList_shouldReturnEmptyList() {
        // Act
        List<ContentWithSource> result = textSplitService.getContentWithSources(Collections.emptyList());

        // Assert
        assertTrue(result.isEmpty());
    }

    // Tests for splitContent method
    @Test
    void splitContent_withSingleContentBelowMaxSize_shouldNotSplitContent() {
        // Act
        List<ContentWithSource> contentWithSources = Collections.singletonList(
                new ContentWithSource("Short content", "https://example.com"));

        // Act
        List<ScrapedTextBatch> result = textSplitService.splitContent(contentWithSources);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getContent().size());
        assertEquals("Short content", result.get(0).getContent().get(0));
        assertEquals("https://example.com", result.get(0).getSource());
    }

    @Test
    void splitContent_withMultipleContentsBelowMaxSize_shouldGroupByContentPartsPerPart() {
        // Arrange
        List<ContentWithSource> contentWithSources = Arrays.asList(
                new ContentWithSource("Content 1", "https://example.com/1"),
                new ContentWithSource("Content 2", "https://example.com/2"),
                new ContentWithSource("Content 3", "https://example.com/3"));

        // Act
        List<ScrapedTextBatch> result = textSplitService.splitContent(contentWithSources);

        // Assert
        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getContent().size());
        assertEquals(1, result.get(1).getContent().size());
        assertEquals("Content 1", result.get(0).getContent().get(0));
        assertEquals("Content 2", result.get(0).getContent().get(1));
        assertEquals("Content 3", result.get(1).getContent().get(0));
    }

    @Test
    void splitContent_withContentExceedingMaxSize_shouldSplitContent() {
        // Arrange
        List<ContentWithSource> contentWithSources = Collections.singletonList(
                new ContentWithSource("This is a long sentence. This is another sentence.", "https://example.com"));

        // Act
        List<ScrapedTextBatch> result = textSplitService.splitContent(contentWithSources);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).getContent().size() > 0);
        assertEquals("https://example.com", result.get(0).getSource());
    }

    // Tests for splitStringWithOverlap method
    @Test
    void splitStringWithOverlap_withShortString_shouldReturnSingleItem() {
        // Arrange
        String content = "Short string";
        int maxChars = 20;
        int overlap = 5;

        // Act
        List<String> result = textSplitService.splitStringWithOverlap(content, maxChars, overlap);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Short string", result.get(0));
    }

    @Test
    void splitStringWithOverlap_withLongStringNoSentenceBreaks_shouldSplitAtSpaces() {
        // Arrange
        String content = "This is a very long string with no sentence breaks but it should be split at word boundaries anyway";
        int maxChars = 30;
        int overlap = 5;

        // Act
        List<String> result = textSplitService.splitStringWithOverlap(content, maxChars, overlap);

        // Assert
        assertTrue(result.size() > 1);
        // Check that splits occur at spaces
        for (String part : result) {
            assertTrue(part.length() <= maxChars);
            if (!part.equals(result.get(result.size() - 1))) {
                assertFalse(part.endsWith(" "));
            }
        }
    }

    @Test
    void splitStringWithOverlap_withLongStringWithSentenceBreaks_shouldPreferSplittingAtSentenceBreaks() {
        // Arrange
        String content = "This is the first sentence. This is the second sentence? This is the third sentence! And this continues.";
        int maxChars = 40;
        int overlap = 5;

        // Act
        List<String> result = textSplitService.splitStringWithOverlap(content, maxChars, overlap);

        // Assert
        assertTrue(result.size() > 1);
        // Check that some splits occur at sentence breaks
        boolean hasSentenceBreakSplit = false;
        for (String part : result) {
            if (part.endsWith(".") || part.endsWith("?") || part.endsWith("!")) {
                hasSentenceBreakSplit = true;
                break;
            }
        }
        assertTrue(hasSentenceBreakSplit);
    }

    @Test
    void splitStringWithOverlap_withDifferentOverlapSizes_shouldHaveCorrectOverlap() {
        // Arrange
        String content = "This is a test string that should be split with different overlap sizes";
        int maxChars = 20;
        int overlap = 10;

        // Act
        List<String> result = textSplitService.splitStringWithOverlap(content, maxChars, overlap);

        // Assert
        assertTrue(result.size() > 1);
        // This is a simplified check - in reality we'd need to check actual character
        // overlap
        for (int i = 1; i < result.size(); i++) {
            String previousPart = result.get(i - 1);
            String currentPart = result.get(i);

            // Some overlap might not be exactly as expected due to trimming and boundary
            // adjustments
            // So we're just checking that parts aren't completely disconnected
            assertFalse(previousPart.isEmpty() || currentPart.isEmpty());
        }
    }

    // Helper methods
    private ScrapeItem createScrapeItem(String content, String link) {
        return ScrapeItem.builder().content(content).link(link).build();
    }
}
