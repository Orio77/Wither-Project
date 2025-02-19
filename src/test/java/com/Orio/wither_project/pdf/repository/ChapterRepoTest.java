package com.Orio.wither_project.pdf.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.repository.ChapterRepo;
import com.Orio.wither_project.summary.repository.PDFRepo;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class ChapterRepoTest {

    @Autowired
    private ChapterRepo chapterRepo;

    @Autowired
    private PDFRepo pdfRepo;

    private DocumentModel testDoc;

    @BeforeEach
    void setUp() {
        testDoc = createTestDocument("Test Document");
        // pdfRepo.save(testDoc);
        createTestChapter(testDoc, 1, "Test Chapter 1");
        createTestChapter(testDoc, 2, "Test Chapter 2");
    }

    @Test
    void verifyDatabaseState() {
        // Verify document exists
        List<DocumentModel> allDocs = pdfRepo.findAll();
        assertThat(allDocs).hasSize(1);
        assertThat(allDocs.get(0).getTitle()).isEqualTo("Test Document");

        // Verify chapters
        List<ChapterModel> allChapters = chapterRepo.findAll();
        assertThat(allChapters).hasSize(2);
        assertThat(allChapters).extracting("title")
                .containsExactlyInAnyOrder("Test Chapter 1", "Test Chapter 2");

        // Verify relationships
        assertThat(allChapters).allMatch(chapter -> chapter.getDoc().getId().equals(testDoc.getId()));
    }

    private DocumentModel createTestDocument(String title) {
        DocumentModel doc = new DocumentModel();
        doc.setTitle(title);
        return doc;
    }

    private ChapterModel createTestChapter(DocumentModel doc, int chapterNumber, String title) {
        ChapterModel chapter = new ChapterModel();
        chapter.setDoc(doc);
        chapter.setChapterNumber(chapterNumber);
        chapter.setTitle(title);
        return chapterRepo.save(chapter);
    }

    @Test
    void findByDocumentTitle_ShouldReturnAllChapters() {
        // when
        List<ChapterModel> found = chapterRepo.findByDocumentTitle("Test Document");

        // then
        assertThat(found).hasSize(2);
        assertThat(found).extracting("title")
                .containsExactlyInAnyOrder("Test Chapter 1", "Test Chapter 2");
    }

    @Test
    void findByDocumentTitle_ShouldReturnEmptyList_WhenNoMatches() {
        // when
        List<ChapterModel> found = chapterRepo.findByDocumentTitle("Nonexistent Document");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByDocumentTitle_ShouldReturnChaptersInOrder() {
        // when
        List<ChapterModel> found = chapterRepo.findByDocumentTitle("Test Document");

        // then
        assertThat(found)
                .isSortedAccordingTo((c1, c2) -> Integer.compare(c1.getChapterNumber(), c2.getChapterNumber()));
    }
}
