package com.Orio.wither_project.pdf.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.PageModel;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class PageRepoTest {

    @Autowired
    private PageRepo pageRepo;

    @Autowired
    private ChapterRepo chapterRepo;

    private ChapterModel testChapter;

    @BeforeEach
    void setUp() {
        testChapter = createTestChapter("Test Chapter");
        createTestPage(testChapter, 1, "Content 1");
        createTestPage(testChapter, 2, "Content 2");
        createTestPage(testChapter, 3, "Content 3");
    }

    @Test
    void verifyDatabaseState() {
        // Verify chapter exists
        List<ChapterModel> allChapters = chapterRepo.findAll();
        assertThat(allChapters).hasSize(1);
        assertThat(allChapters.get(0).getTitle()).isEqualTo("Test Chapter");

        // Verify pages
        List<PageModel> allPages = pageRepo.findAll();
        assertThat(allPages).hasSize(3);
        assertThat(allPages).extracting("content")
                .containsExactlyInAnyOrder("Content 1", "Content 2", "Content 3");

        // Verify relationships
        assertThat(allPages).allMatch(page -> page.getChapter().getId().equals(testChapter.getId()));
    }

    @Test
    void findByChapterTitleOrderByPageNumber_ShouldReturnAllPages() {
        // when
        List<PageModel> found = pageRepo.findByChapterTitleOrderByPageNumber("Test Chapter");

        // then
        assertThat(found).hasSize(3);
        assertThat(found).extracting("content")
                .containsExactly("Content 1", "Content 2", "Content 3");
    }

    @Test
    void findByChapterTitleOrderByPageNumber_ShouldReturnEmptyList_WhenNoMatches() {
        // when
        List<PageModel> found = pageRepo.findByChapterTitleOrderByPageNumber("Nonexistent Chapter");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findByChapterTitleOrderByPageNumber_ShouldReturnPagesInOrder() {
        // when
        List<PageModel> found = pageRepo.findByChapterTitleOrderByPageNumber("Test Chapter");

        // then
        assertThat(found)
                .isSortedAccordingTo((p1, p2) -> Integer.compare(p1.getPageNumber(), p2.getPageNumber()));
    }

    private ChapterModel createTestChapter(String title) {
        ChapterModel chapter = new ChapterModel();
        chapter.setTitle(title);
        return chapterRepo.save(chapter);
    }

    private PageModel createTestPage(ChapterModel chapter, int pageNumber, String content) {
        PageModel page = new PageModel();
        page.setChapter(chapter);
        page.setPageNumber(pageNumber);
        page.setContent(content);
        return pageRepo.save(page);
    }
}
