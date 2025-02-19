package com.Orio.wither_project.pdf.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.repository.PDFRepo;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class PDFRepoTest {

    @Autowired
    private PDFRepo pdfRepo;

    private DocumentModel testDoc;

    @BeforeEach
    void setUp() {
        testDoc = createTestDocument("Test Document", "Test Author");
        pdfRepo.save(testDoc);
    }

    @Test
    void verifyDatabaseState() {
        assertThat(pdfRepo.findAll()).hasSize(1);
        DocumentModel savedDoc = pdfRepo.findAll().get(0);
        assertThat(savedDoc.getTitle()).isEqualTo("Test Document");
        assertThat(savedDoc.getAuthor()).isEqualTo("Test Author");
    }

    @Test
    void findByTitle_ShouldReturnDocument_WhenExists() {
        // when
        Optional<DocumentModel> found = pdfRepo.findByTitle("Test Document");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Document");
        assertThat(found.get().getAuthor()).isEqualTo("Test Author");
    }

    @Test
    void findByTitle_ShouldReturnEmpty_WhenNotExists() {
        // when
        Optional<DocumentModel> found = pdfRepo.findByTitle("Nonexistent Document");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void save_ShouldThrowException_WhenDuplicateTitle() {
        // given
        DocumentModel duplicateDoc = createTestDocument("Test Document", "Another Author");

        // when/then
        assertThrows(DataIntegrityViolationException.class, () -> {
            pdfRepo.save(duplicateDoc);
            pdfRepo.flush();
        });
    }

    private DocumentModel createTestDocument(String title, String author) {
        DocumentModel doc = new DocumentModel();
        doc.setTitle(title);
        doc.setAuthor(author);
        return doc;
    }
}
