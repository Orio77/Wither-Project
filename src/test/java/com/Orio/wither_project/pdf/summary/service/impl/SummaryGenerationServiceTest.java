package com.Orio.wither_project.pdf.summary.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.pdf.summary.model.SummaryType;
import com.Orio.wither_project.pdf.summary.service.IPDFSummaryGenerationService;
import com.Orio.wither_project.pdf.summary.test.TestTextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
class SummaryGenerationServiceTest {

    @Autowired
    private IPDFSummaryGenerationService summaryService;

    @Autowired
    private TestTextConfiguration testText;

    @Test
    void summarize_WithSingleParagraph_ShouldReturnNonEmptySummary() {
        String summary = summaryService.summarize(testText.getSingleParagraph(), SummaryType.PAGE);

        assertNotNull(summary);
        assertFalse(summary.isEmpty());
        assertTrue(summary.length() < testText.getSingleParagraph().length());
    }

    @Test
    void summarize_WithCustomInstruction_ShouldReturnValidSummary() {
        String instruction = "Summarize this text in exactly three sentences.";
        String summary = summaryService.summarize(testText.getShortTechnical(), instruction);

        assertNotNull(summary);
        assertFalse(summary.isEmpty());
        assertTrue(summary.split("\\.").length <= 3);
    }

    @Test
    void summarizeProgressively_WithPageSummaries_ShouldReturnCoherentSummary() {
        String summary = summaryService.summarizeProgressively(testText.getPageSummaries(), SummaryType.CHAPTER);

        assertNotNull(summary);
        assertFalse(summary.isEmpty());
        assertTrue(summary.length() < testText.getPageSummaries().length());
    }

    @Test
    void summarizeProgressively_WithChapterSummaries_ShouldReturnCoherentSummary() {
        String summary = summaryService.summarizeProgressively(testText.getChapterSummaries(), SummaryType.CHAPTER);

        assertNotNull(summary);
        assertFalse(summary.isEmpty());
        assertTrue(summary.length() < testText.getChapterSummaries().length());
    }

    @Test
    void summarize_WithDifferentSummaryTypes_ShouldReturnDifferentResults() {
        String pageSummary = summaryService.summarize(testText.getPageSummaries(), SummaryType.PAGE);
        String chapterSummary = summaryService.summarize(testText.getPageSummaries(), SummaryType.CHAPTER);

        assertNotNull(pageSummary);
        assertNotNull(chapterSummary);
        assertNotEquals(pageSummary, chapterSummary);
    }
}
