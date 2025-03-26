package com.Orio.wither_project.pdf.summary.service.extraction.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.Orio.wither_project.pdf.util.TestUtils;
import com.Orio.wither_project.process.summary.model.ChapterModel;
import com.Orio.wither_project.process.summary.model.PageModel;
import com.Orio.wither_project.process.summary.service.extraction.IPDFChapterExtractionService;
import com.Orio.wither_project.process.summary.service.extraction.impl.BasicContentExtractionService;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicContentExtractionService Tests")
class ContentExtractionServiceTest {

    @Mock
    private IPDFChapterExtractionService chapterExtractionService;

    private BasicContentExtractionService service;

    @BeforeEach
    void setUp() {
        service = new BasicContentExtractionService(chapterExtractionService);
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("getPages should throw IllegalArgumentException when document is null")
        void getPagesThrowsExceptionForNullDocument() {
            assertThrows(IllegalArgumentException.class, () -> service.getPages(null),
                    "getPages should throw IllegalArgumentException for null document");
        }

        @Test
        @DisplayName("getChapters should throw IllegalArgumentException when document is null")
        void getChaptersThrowsExceptionForNullDocument() {
            assertThrows(IllegalArgumentException.class, () -> service.getChapters(null),
                    "getChapters should throw IllegalArgumentException for null document");
        }
    }

    @Nested
    @DisplayName("Content Extraction Tests")
    class ContentExtractionTests {

        @Test
        @DisplayName("getPages should return all pages from chapters")
        void getPagesReturnsAllPages() throws IOException {
            try (PDDocument document = TestUtils.loadTestPdf()) {
                // Prepare test data
                ChapterModel chapter1 = new ChapterModel();
                PageModel page1 = new PageModel();
                page1.setPageNumber(1);
                chapter1.setPages(Collections.singletonList(page1));

                ChapterModel chapter2 = new ChapterModel();
                PageModel page2 = new PageModel();
                page2.setPageNumber(2);
                chapter2.setPages(Collections.singletonList(page2));

                List<ChapterModel> mockChapters = Arrays.asList(chapter1, chapter2);

                // Configure mock
                when(chapterExtractionService.extract(document)).thenReturn(mockChapters);

                // Execute and verify
                List<PageModel> pages = service.getPages(document);

                assertAll(
                        () -> assertEquals(2, pages.size(), "Should return correct number of pages"),
                        () -> assertEquals(1, pages.get(0).getPageNumber(),
                                "First page should have correct page number"),
                        () -> assertEquals(2, pages.get(1).getPageNumber(),
                                "Second page should have correct page number"));

                verify(chapterExtractionService).extract(document);
            }
        }

        @Test
        @DisplayName("getChapters should return all chapters")
        void getChaptersReturnsAllChapters() throws IOException {
            try (PDDocument document = TestUtils.loadTestPdf()) {
                // Prepare test data
                List<ChapterModel> mockChapters = Arrays.asList(
                        new ChapterModel(), new ChapterModel());

                // Configure mock
                when(chapterExtractionService.extract(document)).thenReturn(mockChapters);

                // Execute and verify
                List<ChapterModel> chapters = service.getChapters(document);

                assertEquals(2, chapters.size(), "Should return correct number of chapters");
                verify(chapterExtractionService).extract(document);
            }
        }

        @Test
        @DisplayName("getPages should handle extraction errors")
        void getPagesHandlesExtractionErrors() throws IOException {
            try (PDDocument document = TestUtils.loadTestPdf()) {
                when(chapterExtractionService.extract(document))
                        .thenThrow(new RuntimeException("Extraction failed"));

                assertThrows(RuntimeException.class, () -> service.getPages(document),
                        "Should throw RuntimeException when extraction fails");
            }
        }

        @Test
        @DisplayName("getChapters should handle extraction errors")
        void getChaptersHandlesExtractionErrors() throws IOException {
            try (PDDocument document = TestUtils.loadTestPdf()) {
                when(chapterExtractionService.extract(document))
                        .thenThrow(new RuntimeException("Extraction failed"));

                assertThrows(RuntimeException.class, () -> service.getChapters(document),
                        "Should throw RuntimeException when extraction fails");
            }
        }
    }
}
