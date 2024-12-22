package com.Orio.wither_project.service.data.processing.book.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.model.BookModel;
import com.Orio.wither_project.model.BookSummaryModel;
import com.Orio.wither_project.model.ChapterModel;
import com.Orio.wither_project.model.ChapterSummaryModel;
import com.Orio.wither_project.model.PageModel;
import com.Orio.wither_project.model.PageSummaryModel;
import com.Orio.wither_project.service.data.processing.book.IAIBookProcessingService;
import com.Orio.wither_project.service.data.processing.book.extractor.ChapterExtracttionService;
import com.Orio.wither_project.service.summary.IBookSummaryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OllamaBookProcessingService implements IAIBookProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(OllamaBookProcessingService.class);
    private final ChapterExtracttionService chapterExtractor;
    private final IBookSummaryService bookSummaryService;

    @Override
    public BookModel processPDFDocument(PDDocument doc, String fileName) throws IOException {
        logger.info("Starting to process PDF document: {}", fileName);

        BookModel bookModel = new BookModel();
        logger.debug("Extracting chapters from document");
        List<ChapterModel> chapters = chapterExtractor.getChapters(doc);

        for (ChapterModel chapter : chapters) {
            chapter.setBook(bookModel);
        }
        logger.debug("Found {} chapters in document", chapters.size());

        String author = extractAuthor(doc);
        logger.debug("Extracted author: {}", author);

        bookModel.setTitle(fileName);
        bookModel.setAuthor(author);
        bookModel.setChapters(chapters);

        logger.debug("Generating chapter summaries");
        List<ChapterSummaryModel> chapterSummaries = chapters.stream().map(c -> {
            logger.debug("Generating page summaries for chapter: {}", c.getTitle());
            var pageSummaries = bookSummaryService.getPageSummaries(c.getPages());
            logger.debug("Generated {} page summaries for chapter: {}", pageSummaries.size(), c.getTitle());
            var summary = bookSummaryService.generateChapterSummary(pageSummaries);
            logger.debug("Generated chapter summary for chapter: {}", c.getTitle());
            c.setSummary(summary);
            return summary;
        }).collect(Collectors.toList());
        logger.debug("Generated {} chapter summaries", chapterSummaries.size());

        logger.debug("Generating book summary");
        BookSummaryModel bookSummary = bookSummaryService.getBookSummary(chapterSummaries);
        bookSummary.setBook(bookModel);
        bookModel.setSummary(bookSummary);

        logger.info("Completed processing PDF document: {}", fileName);
        return bookModel;
    }

    private String extractAuthor(PDDocument document) { // TODO Create a IBookDataExtractor that will have this method,
                                                        // and chapters method
        PDDocumentInformation info = document.getDocumentInformation();
        return info != null && info.getAuthor() != null ? info.getAuthor() : "Unknown Author";
    }

    @Override
    public List<ChapterModel> getChapters(PDDocument doc) throws IOException {
        return chapterExtractor.getChapters(doc);
    }

    @Override
    public List<PageSummaryModel> getPageSummaries(List<PageModel> pages) {
        return bookSummaryService.getPageSummaries(pages);
    }

    @Override
    public ChapterSummaryModel generateChapterSummary(List<PageSummaryModel> pageSummaries) {
        return bookSummaryService.generateChapterSummary(pageSummaries);
    }

    @Override
    public BookSummaryModel getBookSummary(List<ChapterSummaryModel> chapterSummaries) {
        return bookSummaryService.getBookSummary(chapterSummaries);
    }
}