package com.Orio.wither_project.pdf.summary.service.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.service.extraction.IPDFContentExtractionService;

@SpringBootTest
@ActiveProfiles("test")
class IPDFContentExtractionServiceTest {

    @Autowired
    private IPDFContentExtractionService contentExtractionService;

    @Autowired
    private PDDocument testDocument;

    @Test
    void getPages_ShouldReturnAllPages() {
        // When
        List<PageModel> pages = contentExtractionService.getPages(testDocument);

        // Then
        assertNotNull(pages);
        assertEquals(testDocument.getNumberOfPages(), pages.size());

        // Verify page numbers are sequential
        for (int i = 0; i < pages.size(); i++) {
            PageModel page = pages.get(i);
            assertEquals(i + 1, page.getPageNumber());
            assertNotNull(page);
            assertFalse(page.getContent().trim().isEmpty());
        }
    }

    @Test
    void getChapters_ShouldReturnAllChapters() {
        // When
        List<ChapterModel> chapters = contentExtractionService.getChapters(testDocument);

        // Then
        assertNotNull(chapters);
        assertFalse(chapters.isEmpty());

        // Verify chapter numbers are sequential
        for (int i = 0; i < chapters.size(); i++) {
            assertEquals(i + 1, chapters.get(i).getChapterNumber());
            assertNotNull(chapters.get(i).getPages());
            assertFalse(chapters.get(i).getPages().isEmpty());
            chapters.get(i).getPages().stream().forEach(page -> {
                assertNotNull(chapters);
                assertFalse(page.getContent().trim().isEmpty());
            });
        }
    }

    @Test
    void getPages_WithNullDocument_ShouldThrowException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> contentExtractionService.getPages(null));
    }

    @Test
    void getChapters_WithNullDocument_ShouldThrowException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> contentExtractionService.getChapters(null));
    }
}
