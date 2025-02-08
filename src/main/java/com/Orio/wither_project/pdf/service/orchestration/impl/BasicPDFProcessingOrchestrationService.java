package com.Orio.wither_project.pdf.service.orchestration.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.Orio.wither_project.pdf.exception.PDFProcessingException;
import com.Orio.wither_project.pdf.model.ChapterModel;
import com.Orio.wither_project.pdf.model.DocumentModel;
import com.Orio.wither_project.pdf.model.PageModel;
import com.Orio.wither_project.pdf.repository.entity.FileEntity;
import com.Orio.wither_project.pdf.service.conversion.IPDFConversionService;
import com.Orio.wither_project.pdf.service.extraction.IPDFContentExtractionService;
import com.Orio.wither_project.pdf.service.extraction.IPDFMetaDataExtractionService;
import com.Orio.wither_project.pdf.service.orchestration.IPDFProcessingOrchestrationService;
import com.Orio.wither_project.pdf.service.storage.ISQLDocumentService;
import com.Orio.wither_project.pdf.summary.model.BookSummaryModel;
import com.Orio.wither_project.pdf.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.pdf.summary.model.PageSummaryModel;
import com.Orio.wither_project.pdf.summary.service.IPDFSummaryGenerationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicPDFProcessingOrchestrationService implements IPDFProcessingOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(BasicPDFProcessingOrchestrationService.class);

    private final ISQLDocumentService pdfSavingService;
    private final IPDFMetaDataExtractionService metaDataExtractionService;
    private final IPDFSummaryGenerationService summaryGenerationService;
    private final IPDFContentExtractionService contentExtractionService;
    private final IPDFConversionService conversionService;

    @Override
    public DocumentModel convert(FileEntity file) throws PDFProcessingException {
        Assert.notNull(file, "File entity cannot be null");
        logger.info("Starting PDF conversion for file: {}", file.getName());

        try (PDDocument doc = conversionService.convertToPdDocument(file)) {
            DocumentModel docModel = new DocumentModel();
            List<ChapterModel> chapters = contentExtractionService.getChapters(doc);
            docModel.addChapters(chapters);

            // TODO Remove that and put that into setMetadata method
            docModel.setAuthor(metaDataExtractionService.getAuthor(doc));
            docModel.setTitle(metaDataExtractionService.getTitle(doc));
            docModel.setFileName(metaDataExtractionService.getFileName(doc));

            logger.info("PDF conversion completed for file: {}", file.getName());
            return docModel;
        } catch (IOException e) {
            throw new PDFProcessingException("Failed to process PDF file: " + file.getName(), e);
        }
    }

    @Override
    public boolean setMetadata(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.hasText(model.getFileName(), "File name cannot be empty");
        logger.info("Setting metadata for document: {}", model.getFileName());
        return true;
    }

    @Override
    public boolean setContents(DocumentModel model) {
        return true;
    }

    @Override
    public void save(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.notNull(model.getChapters(), "Chapters cannot be null");
        logger.info("Saving document: {}", model.getFileName());
        pdfSavingService.saveDoc(model);
        logger.info("Document saved successfully: {}", model.getFileName());

        logger.info("Saving chapters for document: {}", model.getFileName());
        List<ChapterModel> chapters = model.getChapters();
        pdfSavingService.saveChapters(chapters);
        logger.info("{} Chapters saved successfully for document: {}", chapters.size(), model.getFileName());

        AtomicInteger totalPageCount = new AtomicInteger(0);

        chapters.forEach(chapter -> {
            logger.info("Saving pages for chapter: {}", chapter.getTitle());
            pdfSavingService.savePages(chapter.getPages());
            totalPageCount.addAndGet(chapter.getPages().size());
            logger.info("{} Pages saved successfully for chapter: {}", chapter.getPages().size(), chapter.getTitle());
        });

        logger.info("{} Pages saved successfully for document: {}", totalPageCount.get(), model.getFileName());
    }

    @Override
    public boolean setSummaries(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.notNull(model.getChapters(), "Chapters cannot be null");

        logger.info("Setting summaries for document: {}", model.getFileName());
        try {
            List<ChapterModel> chapters = model.getChapters();
            if (chapters == null || chapters.isEmpty()) {
                throw new PDFProcessingException("No chapters found for document");
            }

            generateAndSavePageSummaries(extractPages(chapters));
            generateAndSaveChapterSummaries(chapters);
            generateAndSaveBookSummary(model, chapters);

            pdfSavingService.saveDoc(model);

            logger.info("Summaries set successfully for document: {}", model.getFileName());
            return true;
        } catch (Exception e) {
            logger.error("Error setting summaries for document: {}", model.getFileName(), e);
            throw new PDFProcessingException("Failed to set summaries", e);
        }
    }

    private List<PageModel> extractPages(List<ChapterModel> chapters) {
        return chapters.stream()
                .flatMap(chapter -> chapter.getPages().stream())
                .collect(Collectors.toList());
    }

    private void generateAndSavePageSummaries(List<PageModel> pages) {
        logger.debug("Generating and saving page summaries");

        summaryGenerationService.generatePageSummaries(pages);

        logger.debug("Page summaries generated and saved successfully");
    }

    private void generateAndSaveChapterSummaries(List<ChapterModel> chapters) {
        logger.info("Generating and saving chapter summaries");
        chapters.forEach(chapter -> {
            String pageSummaries = chapter.getPages().stream()
                    .map(PageModel::getSummary)
                    .map(PageSummaryModel::getContent)
                    .collect(Collectors.joining("\n\n\n\n"));
            String chapterSummary = summaryGenerationService.summarizeChapter(pageSummaries);
            ChapterSummaryModel chapterSummaryModel = new ChapterSummaryModel(chapterSummary);
            chapterSummaryModel.setChapter(chapter);
            chapter.setSummary(chapterSummaryModel);
        });

        logger.info("Chapter summaries generated and saved successfully");
    }

    private void generateAndSaveBookSummary(DocumentModel model, List<ChapterModel> chapters) {
        logger.info("Generating and saving book summary for document: {}", model.getFileName());
        String chapterSummaries = chapters.stream()
                .map(ChapterModel::getSummary)
                .map(ChapterSummaryModel::getContent)
                .collect(Collectors.joining("\n\n\n\n"));
        String bookSummary = summaryGenerationService.summarizeDocument(chapterSummaries);
        BookSummaryModel bookSummaryModel = new BookSummaryModel(bookSummary);
        model.setSummary(bookSummaryModel);
        bookSummaryModel.setBook(model);

        logger.info("Book summary generated and saved successfully for document: {}", model.getFileName());
    }
}
