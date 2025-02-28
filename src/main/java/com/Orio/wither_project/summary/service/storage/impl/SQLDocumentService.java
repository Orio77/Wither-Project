package com.Orio.wither_project.summary.service.storage.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.pdf.repository.PDFRepo;
import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.repository.ChapterRepo;
import com.Orio.wither_project.summary.repository.DocumentModelRepo;
import com.Orio.wither_project.summary.repository.PageRepo;
import com.Orio.wither_project.summary.service.storage.ISQLDocumentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SQLDocumentService implements ISQLDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(SQLDocumentService.class);

    private final ChapterRepo chapterRepo;
    private final PDFRepo fileRepo;
    private final PageRepo pageRepo;
    private final DocumentModelRepo pdfRepo;

    public boolean savePage(PageModel page) {
        logger.info("Saving page: {}", page);
        boolean result = pageRepo.saveAndFlush(page) != null;
        logger.info("Page saved: {}", result);
        return result;
    }

    @Override
    public boolean savePages(List<PageModel> pages) {
        logger.info("Saving {} pages", pages.size());
        boolean result = pageRepo.saveAllAndFlush(pages) != null;
        logger.info("Pages saved: {}", result);
        return result;
    }

    @Override
    public List<PageModel> getPages(String chapterTitle) {
        logger.info("Retrieving pages for chapter: {}", chapterTitle);
        try {
            List<PageModel> pages = pageRepo.findByChapterTitleOrderByPageNumber(chapterTitle);
            logger.info("Found {} pages for chapter: {}", pages.size(), chapterTitle);
            return pages;
        } catch (Exception e) {
            logger.error("Error retrieving pages for chapter: {}", chapterTitle, e);
            return null;
        }
    }

    public List<PageModel> getUnprocessedPagesByChapter(String chapterTitle) {
        logger.info("Retrieving unprocessed pages for chapter: {}", chapterTitle);
        try {
            List<PageModel> pages = pageRepo.findByChapterTitleAndSummaryIdIsNullOrderByPageNumber(chapterTitle);
            logger.info("Found {} unprocessed pages for chapter: {}", pages.size(), chapterTitle);
            return pages;
        } catch (Exception e) {
            logger.error("Error retrieving unprocessed pages for chapter: {}", chapterTitle, e);
            return null;
        }
    }

    @Override
    public boolean saveChapters(List<ChapterModel> chapters) {
        logger.info("Saving {} chapters", chapters.size());
        boolean result = chapterRepo.saveAllAndFlush(chapters) != null;
        logger.info("Chapters saved: {}", result);
        return result;
    }

    @Override
    public List<ChapterModel> getChapters(String documentTitle) {
        logger.info("Retrieving chapters for document: {}", documentTitle);
        try {
            List<ChapterModel> chapters = chapterRepo.findByDocumentTitle(documentTitle);
            logger.info("Found {} chapters for document: {}", chapters.size(), documentTitle);
            return chapters;
        } catch (Exception e) {
            logger.error("Error retrieving chapters for document: {}", documentTitle, e);
            return null;
        }
    }

    public List<ChapterModel> getUnprocessedChaptersByDocument(String documentTitle) {
        logger.info("Retrieving unprocessed chapters for document: {}", documentTitle);
        try {
            List<ChapterModel> chapters = chapterRepo
                    .findUnsummarizedChapters(documentTitle);
            logger.info("Found {} unprocessed chapters for document: {}", chapters.size(), documentTitle);
            return chapters;
        } catch (Exception e) {
            logger.error("Error retrieving unprocessed chapters for document: {}", documentTitle, e);
            return null;
        }
    }

    @Override
    public boolean saveDoc(DocumentModel documentModel) {
        logger.info("Saving document: {}", documentModel.getTitle());
        boolean result = pdfRepo.saveAndFlush(documentModel) != null;
        logger.info("Document saved: {}", result);
        return result;
    }

    @Override
    public DocumentModel getDocument(String title) {
        logger.info("Retrieving document: {}", title);
        try {
            Optional<DocumentModel> optionalDocument = pdfRepo.findByTitle(title);
            if (optionalDocument.isEmpty()) {
                logger.warn("Document not found by title: {}", title);

                optionalDocument = pdfRepo.findByFileName(title); // TODO nested if's can be improved. Change when
                                                                  // option for document retrieval by name is added
                if (optionalDocument.isEmpty()) {
                    logger.warn("Document not found by file name: {}", title);
                    return null;
                }
            }
            return optionalDocument.get();
        } catch (Exception e) {
            logger.error("Error retrieving document: {}", title, e);
            return null;
        }
    }

    @Override
    public List<DocumentModel> getAllDocs() {
        logger.info("Retrieving all PDF files");
        try {
            return pdfRepo.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all PDF files", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteDoc(String title) {
        logger.info("Calling repository to delete {}", title);
        boolean deletedDoc = pdfRepo.deleteByTitleOrFileName(title);
        logger.info("Successful deletion of the document: {}", deletedDoc);
        boolean deletedFile = (fileRepo.deleteByName(title) > 0);
        logger.info("Successful deletion of the file: {}", deletedFile);
    }
}
