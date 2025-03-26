package com.Orio.wither_project.pdf.summary.service.generation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.config.TestTextConfiguration;
import com.Orio.wither_project.process.summary.model.DocumentModel;
import com.Orio.wither_project.process.summary.model.PageModel;
import com.Orio.wither_project.process.summary.model.PageSummaryModel;
import com.Orio.wither_project.process.summary.model.SummaryType;
import com.Orio.wither_project.process.summary.service.conversion.IPDFConversionService;
import com.Orio.wither_project.process.summary.service.generation.IPDFParallelSummaryGenerationService;
import com.Orio.wither_project.process.summary.service.generation.IPDFSummaryGenerationService;

@SpringBootTest
@ActiveProfiles("test")
class SummaryGenerationServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SummaryGenerationServiceTest.class);

    @Autowired
    private IPDFSummaryGenerationService summaryService;

    @Autowired
    private TestTextConfiguration testText;

    @Autowired
    private IPDFParallelSummaryGenerationService parallelSummaryService;

    @Autowired
    private PDDocument testPDDocument;

    @Autowired
    private IPDFConversionService conversionService;

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

    @Test
    void measureSequentialVsParallelPerformance() throws IOException {
        // Convert PDF to DocumentModel
        DocumentModel document = conversionService.convertToDocumentModel(testPDDocument);
        List<PageModel> pages = document.getChapters().stream().flatMap(ch -> ch.getPages().stream()).toList();

        // Sequential processing
        long startSequential = System.currentTimeMillis();
        List<PageSummaryModel> seqResult = summaryService.generatePageSummaries(pages);
        long sequentialTime = System.currentTimeMillis() - startSequential;
        logger.info("Sequential generation took: {}ms", sequentialTime);

        // Parallel processing
        long startParallel = System.currentTimeMillis();
        List<PageSummaryModel> parResult = parallelSummaryService.generatePageSummaries(pages);
        long parallelTime = System.currentTimeMillis() - startParallel;
        logger.info("Parallel generation took: {}ms", parallelTime);

        assertFalse(seqResult.isEmpty());
        assertFalse(parResult.isEmpty());
        assertEquals(pages.size(), seqResult.size());
        assertEquals(pages.size(), parResult.size());
    }
}
