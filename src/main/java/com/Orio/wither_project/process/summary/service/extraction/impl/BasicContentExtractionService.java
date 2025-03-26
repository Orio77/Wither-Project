package com.Orio.wither_project.process.summary.service.extraction.impl;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.process.summary.model.ChapterModel;
import com.Orio.wither_project.process.summary.model.PageModel;
import com.Orio.wither_project.process.summary.service.extraction.IPDFChapterExtractionService;
import com.Orio.wither_project.process.summary.service.extraction.IPDFContentExtractionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicContentExtractionService implements IPDFContentExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(BasicContentExtractionService.class);
    private final IPDFChapterExtractionService chapterExtractionService;

    @Override
    public List<PageModel> getPages(PDDocument doc) {
        if (doc == null) {
            logger.error("PDDocument cannot be null");
            throw new IllegalArgumentException("PDDocument cannot be null");
        }

        logger.debug("Starting page extraction from PDF document");
        try {
            List<ChapterModel> chapters = chapterExtractionService.extract(doc);
            List<PageModel> pages = chapters.stream()
                    .flatMap(chapter -> chapter.getPages().stream())
                    .toList();
            logger.info("Successfully extracted {} pages from PDF document", pages.size());
            return pages;
        } catch (Exception e) {
            logger.error("Failed to extract pages from PDF document", e);
            throw new RuntimeException("Failed to extract pages from PDF document", e);
        }
    }

    @Override
    public List<ChapterModel> getChapters(PDDocument doc) {
        if (doc == null) {
            logger.error("PDDocument cannot be null");
            throw new IllegalArgumentException("PDDocument cannot be null");
        }

        logger.debug("Starting chapter extraction from PDF document");
        try {
            List<ChapterModel> chapters = chapterExtractionService.extract(doc);
            logger.info("Successfully extracted {} chapters from PDF document", chapters.size());
            return chapters;
        } catch (Exception e) {
            logger.error("Failed to extract chapters from PDF document", e);
            throw new RuntimeException("Failed to extract chapters from PDF document", e);
        }
    }

}
