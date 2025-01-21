package com.Orio.wither_project.pdf.service.extraction;

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

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.PageModel;

@SpringBootTest
@ActiveProfiles("test")
class IPDFChapterExtractionServiceTest {

    @Autowired
    private PDDocument testDocument;

    @Autowired
    private IPDFChapterExtractionService chapterExtractionService;

    @Test
    void extract_ShouldReturnChaptersWithPages() {
        // When
        List<ChapterModel> chapters = chapterExtractionService.extract(testDocument);

        // Then
        assertNotNull(chapters);
        assertFalse(chapters.isEmpty());

        int totalPages = 0;
        for (int i = 0; i < chapters.size(); i++) {
            ChapterModel chapter = chapters.get(i);
            assertEquals(i + 1, chapter.getChapterNumber());
            assertNotNull(chapter.getTitle());
            assertNotNull(chapter.getPages());

            // Verify pages in chapter
            List<PageModel> pages = chapter.getPages();
            assertFalse(pages.isEmpty());
            totalPages += pages.size();

            // Verify page-chapter relationship
            for (PageModel page : pages) {
                assertEquals(chapter, page.getChapter());
            }
        }

        // Verify all document pages are accounted for
        assertEquals(testDocument.getNumberOfPages(), totalPages);
    }

    @Test
    void extract_WithNullDocument_ShouldThrowException() {
        // When/Then
        assertThrows(NullPointerException.class, () -> chapterExtractionService.extract(null));
    }
}
