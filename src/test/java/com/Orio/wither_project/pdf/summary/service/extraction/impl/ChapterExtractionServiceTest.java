package com.Orio.wither_project.pdf.summary.service.extraction.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.Orio.wither_project.pdf.util.TestUtils;
import com.Orio.wither_project.process.summary.model.ChapterModel;
import com.Orio.wither_project.process.summary.service.extraction.impl.BasicChapterExtractionService;

@DisplayName("BasicChapterExtractionService Tests")
class ChapterExtractionServiceTest {

    private BasicChapterExtractionService service;

    @BeforeEach
    void setUp() {
        service = new BasicChapterExtractionService();
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {
        @Test
        @DisplayName("Should throw NullPointerException when document is null")
        void shouldThrowExceptionForNullDocument() {
            assertThrows(NullPointerException.class, () -> service.extract(null),
                    "Extract should throw NullPointerException for null document");
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        @DisplayName("Should handle invalid page counts")
        void shouldHandleInvalidPageCounts(int pageCount) throws IOException {
            try (PDDocument document = TestUtils.loadTestPdf()) {
                // Simulate invalid page count by creating new empty document
                PDDocument emptyDoc = new PDDocument();
                List<ChapterModel> chapters = service.extract(emptyDoc);
                assertTrue(chapters.isEmpty(), "Should return empty list for invalid page counts");
                emptyDoc.close();
            }
        }
    }

    @Nested
    @DisplayName("Chapter Extraction Tests")
    class ChapterExtractionTests {
        @Test
        @DisplayName("Should correctly extract chapters from test document")
        void shouldExtractChapters() throws IOException {
            try (PDDocument document = TestUtils.loadTestPdf()) {
                List<ChapterModel> chapters = service.extract(document);

                assertAll(
                        "Verify chapter extraction",
                        () -> assertFalse(chapters.isEmpty(), "Should extract chapters"),
                        () -> assertTrue(chapters.get(0).getPages().size() > 0, "Chapters should have pages"),
                        () -> assertNotNull(chapters.get(0).getTitle(), "Chapters should have titles"),
                        () -> assertFalse(chapters.get(0).getPages().get(0).getContent().isEmpty(),
                                "Pages should have content"));
            }
        }

        @Test
        @DisplayName("Should handle single page document")
        void shouldHandleSinglePageDocument() throws IOException {
            try (PDDocument document = TestUtils.loadTestPdf()) {
                List<ChapterModel> chapters = service.extract(document);

                assertAll(
                        "Verify single page document handling",
                        () -> assertFalse(chapters.isEmpty(), "Should create at least one chapter"),
                        () -> assertTrue(chapters.get(0).getPages().size() > 0, "Chapter should have pages"),
                        () -> assertFalse(chapters.get(0).getPages().get(0).getContent().isEmpty(),
                                "Page should have content"));
            }
        }
    }
}
