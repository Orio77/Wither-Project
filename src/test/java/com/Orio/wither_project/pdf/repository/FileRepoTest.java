package com.Orio.wither_project.pdf.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.Orio.wither_project.pdf.repository.entity.FileEntity;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class FileRepoTest {

    @Autowired
    private FilePDFRepo fileRepo;

    private FileEntity testFile;

    @BeforeEach
    void setUp() {
        testFile = createTestFile("test-file.pdf", "Test Content");
        fileRepo.save(testFile);
    }

    @Test
    void findByName_ShouldReturnFile_WhenFileExists() {
        // when
        FileEntity found = fileRepo.findByName("test-file.pdf");

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("test-file.pdf");
        assertThat(new String(found.getData())).isEqualTo("Test Content");
    }

    @Test
    void findByName_ShouldReturnNull_WhenFileDoesNotExist() {
        // when
        FileEntity found = fileRepo.findByName("nonexistent.pdf");

        // then
        assertThat(found).isNull();
    }

    @Test
    void save_ShouldPersistFile() {
        // given
        FileEntity newFile = createTestFile("new-file.pdf", "New Content");

        // when
        FileEntity saved = fileRepo.save(newFile);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("new-file.pdf");
        assertThat(new String(saved.getData())).isEqualTo("New Content");
    }

    private FileEntity createTestFile(String name, String content) {
        FileEntity file = new FileEntity();
        file.setName(name);
        file.setData(content.getBytes());
        return file;
    }
}
