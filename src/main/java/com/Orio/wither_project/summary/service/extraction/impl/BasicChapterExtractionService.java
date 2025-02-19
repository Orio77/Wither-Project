package com.Orio.wither_project.summary.service.extraction.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.service.extraction.IPDFChapterExtractionService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BasicChapterExtractionService implements IPDFChapterExtractionService {
    private static final int DEFAULT_PAGES_PER_CHAPTER = 20;

    @Override
    public List<ChapterModel> extract(final PDDocument doc) {
        Objects.requireNonNull(doc, "PDDocument cannot be null");

        log.info("Starting PDF document processing for extraction");
        log.debug("Document properties: {} pages", doc.getNumberOfPages());

        final List<ChapterModel> chapters = new ArrayList<>();
        final int totalPages = doc.getNumberOfPages();
        final int numberOfChapters = (int) Math.ceil((double) totalPages / DEFAULT_PAGES_PER_CHAPTER);

        log.debug("Calculated {} chapters based on {} pages per chapter", numberOfChapters, DEFAULT_PAGES_PER_CHAPTER);

        for (int i = 0; i < numberOfChapters; i++) {
            log.debug("Starting creation process for chapter {} of {}", i + 1, numberOfChapters);
            chapters.add(createChapter(i, totalPages, doc));
        }

        log.info("PDF extraction completed. Generated {} chapters with total {} pages", chapters.size(), totalPages);
        return chapters;
    }

    private ChapterModel createChapter(final int chapterIndex, final int totalPages, PDDocument doc) {
        log.debug("Creating chapter with index {}", chapterIndex);

        final ChapterModel chapter = new ChapterModel();
        final int chapterNumber = chapterIndex + 1;
        chapter.setTitle("Chapter " + chapterNumber);
        chapter.setChapterNumber(chapterNumber);

        final List<PageModel> pages = new ArrayList<>();
        final int startPage = chapterIndex * DEFAULT_PAGES_PER_CHAPTER;
        final int endPage = Math.min((chapterIndex + 1) * DEFAULT_PAGES_PER_CHAPTER, totalPages);

        log.debug("Starting creation process of pages {} to {} for chapter {}", startPage + 1, endPage, chapterNumber);

        for (int pageNum = startPage; pageNum < endPage; pageNum++) {
            pages.add(createPage(pageNum, chapter, doc));
        }

        chapter.addPages(pages);
        log.debug("Completed chapter {} creation with {} pages", chapterNumber, pages.size());
        return chapter;
    }

    private PageModel createPage(final int pageNum, final ChapterModel chapter, PDDocument doc) {
        log.debug("Extracting content from page {}", pageNum + 1);

        final PageModel page = new PageModel();
        page.setPageNumber(pageNum + 1);

        try {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(pageNum + 1);
            stripper.setEndPage(pageNum + 1);
            String pageContent = stripper.getText(doc);
            page.setContent(pageContent.trim());
            log.debug("Successfully extracted {} characters from page {}", pageContent.length(), pageNum + 1);
        } catch (IOException e) {
            log.error("Failed to extract text from page {}. Error: {}", pageNum + 1, e.getMessage(), e);
            page.setContent("");
        }

        return page;
    }
}
