package com.Orio.wither_project.pdf.service.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.pdf.model.PageModel;
import com.Orio.wither_project.pdf.repository.ChapterRepo;
import com.Orio.wither_project.pdf.repository.PDFRepo;
import com.Orio.wither_project.pdf.repository.PageRepo;
import com.Orio.wither_project.pdf.service.storage.impl.SQLDocumentService;

@SpringBootTest
@ActiveProfiles("test")
public class SQLDocumentServiceTest {

    @Autowired
    private SQLDocumentService sqlDocumentService;

    @Autowired
    private PDFRepo pdfRepo;

    @Autowired
    private ChapterRepo chapterRepo;

    @Autowired
    private PageRepo pageRepo;

    @Test
    void whenDocumentDeletedAllRelatedEntitiesAreDeleted() {
        // Create test data
        DocumentModel document = new DocumentModel();
        document.setTitle("Test Document");
        document.setFileName("test.pdf");

        ChapterModel chapter1 = new ChapterModel();
        chapter1.setTitle("Chapter 1");
        chapter1.setDoc(document);

        ChapterModel chapter2 = new ChapterModel();
        chapter2.setTitle("Chapter 2");
        chapter2.setDoc(document);

        PageModel page1 = new PageModel();
        page1.setPageNumber(1);
        page1.setContent("Page 1 content");

        PageModel page2 = new PageModel();
        page2.setPageNumber(2);
        page2.setContent("Page 2 content");

        chapter1.setPages(List.of(page1, page2));
        page1.setChapter(chapter1);
        page2.setChapter(chapter1);

        PageModel page3 = new PageModel();
        page3.setPageNumber(1);
        page3.setContent("Page 3 content");

        chapter2.setPages(List.of(page3));

        page3.setChapter(chapter2);

        document.setChapters(List.of(chapter1, chapter2));

        // Save all entities
        sqlDocumentService.saveDoc(document);

        DocumentModel doc = sqlDocumentService.getDocument(document.getTitle());

        // Verify data is saved
        assertNotNull(doc);
        assertEquals(2, sqlDocumentService.getChapters(document.getTitle()).size());
        assertEquals(2, sqlDocumentService.getPages(chapter1.getTitle()).size());
        assertEquals(1, sqlDocumentService.getPages(chapter2.getTitle()).size());

        // Delete document
        sqlDocumentService.deleteDoc(document.getTitle());

        // Verify cascade deletion
        assertNull(sqlDocumentService.getDocument(document.getTitle()));
        assertTrue(sqlDocumentService.getChapters(document.getTitle()).isEmpty());
        assertTrue(sqlDocumentService.getPages(chapter1.getTitle()).isEmpty());
        assertTrue(sqlDocumentService.getPages(chapter2.getTitle()).isEmpty());

        // Verify directly with repositories
        assertEquals(0, pdfRepo.count());
        assertEquals(0, chapterRepo.count());
        assertEquals(0, pageRepo.count());
    }
}