package com.Orio.wither_project.pdf.service.orchestration.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.Orio.wither_project.pdf.repository.entity.FileEntity;
import com.Orio.wither_project.pdf.util.TestUtils;
import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.service.conversion.impl.BasicPDFConversionService;
import com.Orio.wither_project.summary.service.extraction.impl.ApachePDFBoxMetaDataExtractionService;
import com.Orio.wither_project.summary.service.extraction.impl.BasicContentExtractionService;
import com.Orio.wither_project.summary.service.orchestration.impl.BasicPDFProcessingOrchestrationService;
import com.Orio.wither_project.summary.service.storage.impl.SQLDocumentService;
import com.Orio.wither_project.summary.summary.service.impl.OllamaSummaryGenerationService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("BasicPDFProcessingOrchestrationService Tests")
class PDFProcessingOrchestrationServiceTest {

    private BasicPDFProcessingOrchestrationService service;
    private FileEntity testFile;

    @Autowired
    private SQLDocumentService sqlDocumentService;

    @Autowired
    private ApachePDFBoxMetaDataExtractionService metaDataExtractionService;

    @Autowired
    private OllamaSummaryGenerationService summaryGenerationService;

    @Autowired
    private BasicPDFConversionService pdfConversionService;

    @Autowired
    private BasicContentExtractionService contentExtractionService;

    @BeforeEach
    void setUp() throws IOException {
        service = new BasicPDFProcessingOrchestrationService(
                sqlDocumentService,
                metaDataExtractionService,
                summaryGenerationService,
                contentExtractionService,
                pdfConversionService);

        PDDocument pdfDocument = TestUtils.loadTestPdf();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pdfDocument.save(baos);
        pdfDocument.close();

        testFile = new FileEntity();
        testFile.setName("test.pdf");
        testFile.setContentType("application/pdf");
        testFile.setData(baos.toByteArray());
    }

    @Nested
    @DisplayName("Document Conversion Tests")
    class DocumentConversionTests {
        @Test
        @DisplayName("Should successfully convert valid PDF")
        void shouldConvertValidPDF() throws IOException {
            DocumentModel result = service.convert(testFile);

            assertAll(
                    "Verify document conversion",
                    () -> assertNotNull(result, "Converted document should not be null"),
                    () -> assertNotNull(result.getTitle(), "Document should have a title"),
                    () -> assertFalse(result.getChapters().isEmpty(), "Document should have chapters"),
                    () -> assertTrue(result.getChapters().get(0).getPages().size() > 0,
                            "Chapters should have pages"));
        }

        @Test
        @DisplayName("Should throw exception for null file")
        void shouldThrowExceptionForNullFile() {
            assertThrows(IllegalArgumentException.class, () -> service.convert(null));
        }
    }

    @Nested
    @DisplayName("Content Processing Tests")
    class ContentProcessingTests {
        @Test
        @DisplayName("Should process document contents")
        void shouldProcessContents() throws IOException {
            DocumentModel document = service.convert(testFile);
            boolean result = service.setContents(document);

            assertTrue(result, "Content processing should succeed");
            assertAll(
                    "Verify processed content",
                    () -> assertNotNull(document.getChapters(), "Should have chapters"),
                    () -> assertTrue(document.getChapters().stream()
                            .allMatch(chapter -> !chapter.getPages().isEmpty()),
                            "All chapters should have pages"),
                    () -> assertTrue(document.getChapters().stream()
                            .flatMap(chapter -> chapter.getPages().stream())
                            .allMatch(page -> page.getContent() != null && !page.getContent().isEmpty()),
                            "All pages should have content"));
        }
    }

    @Nested
    @DisplayName("Summary Generation Tests")
    class SummaryGenerationTests {
        @Test
        @DisplayName("Should generate summaries for document")
        void shouldGenerateSummaries() throws IOException {
            DocumentModel document = service.convert(testFile);
            service.setContents(document);
            boolean result = service.setSummaries(document);

            assertTrue(result, "Summary generation should succeed");
            assertNotNull(document, "Document should not be null");

            // First verify document summary
            assertNotNull(document.getSummary(), "Document should have summary");

            // Then verify chapters if they exist
            List<ChapterModel> chapters = document.getChapters();
            assertNotNull(chapters, "Chapters list should not be null");
            assertFalse(chapters.isEmpty(), "Document should have at least one chapter");

            // Verify chapter summaries
            for (ChapterModel chapter : chapters) {
                assertNotNull(chapter.getSummary(), "Chapter should have summary");

                // Verify page summaries
                List<PageModel> pages = chapter.getPages();
                assertNotNull(pages, "Pages list should not be null");
                assertFalse(pages.isEmpty(), "Chapter should have at least one page");

                for (PageModel page : pages) {
                    assertNotNull(page.getSummary(), "Page should have summary");
                }
            }
        }
    }
}
