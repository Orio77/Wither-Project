package com.Orio.wither_project.summary.service.orchestration.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.Orio.wither_project.pdf.model.entity.FileEntity;
import com.Orio.wither_project.socket.summary.model.ProgressCallback;
import com.Orio.wither_project.socket.summary.service.SummaryProgressService;
import com.Orio.wither_project.summary.exception.PDFProcessingException;
import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.service.conversion.IPDFConversionService;
import com.Orio.wither_project.summary.service.extraction.IPDFContentExtractionService;
import com.Orio.wither_project.summary.service.extraction.IPDFMetaDataExtractionService;
import com.Orio.wither_project.summary.service.generation.IPDFSummaryGenerationService;
import com.Orio.wither_project.summary.service.orchestration.IPDFProcessingOrchestrationService;
import com.Orio.wither_project.summary.service.storage.ISQLDocumentService;
import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicPDFProcessingOrchestrationService implements IPDFProcessingOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(BasicPDFProcessingOrchestrationService.class);

    private final ISQLDocumentService sqlDocumentService;
    private final IPDFMetaDataExtractionService metaDataExtractionService;
    private final IPDFSummaryGenerationService summaryGenerationService;
    private final IPDFContentExtractionService contentExtractionService;
    private final IPDFConversionService conversionService;
    private final SummaryProgressService progressService;

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
        sqlDocumentService.saveDoc(model);
        logger.info("Document saved successfully: {}", model.getFileName());

        logger.info("Saving chapters for document: {}", model.getFileName());
        List<ChapterModel> chapters = model.getChapters();
        sqlDocumentService.saveChapters(chapters);
        logger.info("{} Chapters saved successfully for document: {}", chapters.size(), model.getFileName());

        AtomicInteger totalPageCount = new AtomicInteger(0);

        chapters.forEach(chapter -> {
            logger.info("Saving pages for chapter: {}", chapter.getTitle());
            sqlDocumentService.savePages(chapter.getPages());
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
            generateAndSaveBookSummary(model);

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

        final int BATCH_SIZE = 5;
        final int totalPages = pages.size();
        AtomicInteger processedPages = new AtomicInteger(0);

        List<List<PageModel>> batches = Lists.partition(pages, BATCH_SIZE);

        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = batches.stream()
                .map(batch -> CompletableFuture.runAsync(() -> {
                    summaryGenerationService.generatePageSummaries(batch);
                    sqlDocumentService.savePages(batch);

                    int currentProgress = processedPages.addAndGet(batch.size());
                    double progress = (double) currentProgress / totalPages;
                    progressService.updateProgress(progress);

                    logger.debug("Processed batch of {} pages. Progress: {}/{}",
                            batch.size(), currentProgress, totalPages);
                }, ForkJoinPool.commonPool()))
                .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(futures).get();
            logger.debug("Page summaries generated and saved successfully");
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new PDFProcessingException("Failed to generate page summaries", e);
        }
    }

    // private void generateAndSaveChapterSummariesOld(List<ChapterModel> chapters)
    // {
    // logger.info("Generating and saving chapter summaries");
    // chapters.forEach(chapter -> {
    // String pageSummaries = chapter.getPages().stream()
    // .map(PageModel::getSummary)
    // .map(PageSummaryModel::getContent)
    // .collect(Collectors.joining(constantsConfig.getSplitRegex()));
    // String chapterSummary =
    // summaryGenerationService.summarizeChapter(pageSummaries);
    // ChapterSummaryModel chapterSummaryModel = new
    // ChapterSummaryModel(chapterSummary);
    // chapterSummaryModel.setChapter(chapter);
    // chapter.setSummary(chapterSummaryModel);
    // });

    // logger.info("Chapter summaries generated and saved successfully");
    // }

    private void generateAndSaveChapterSummaries(List<ChapterModel> chapters) {
        logger.info("Generating and saving chapter summaries");

        ProgressCallback progressCallback = (progress) -> {
            progressService.updateProgress(progress);
            logger.debug("Chapter summary progress: {}", progress);
        };

        summaryGenerationService.generateChapterSummaries(chapters, progressCallback);

        sqlDocumentService.saveChapters(chapters);
        logger.info("Chapter summaries generated and saved successfully");
    }

    // private void generateAndSaveBookSummaryOld(DocumentModel model,
    // List<ChapterModel> chapters) {
    // logger.info("Generating and saving book summary for document: {}",
    // model.getFileName());
    // String chapterSummaries = chapters.stream()
    // .map(ChapterModel::getSummary)
    // .map(ChapterSummaryModel::getContent)
    // .collect(Collectors.joining(constantsConfig.getSplitRegex()));
    // String bookSummary =
    // summaryGenerationService.summarizeDocument(chapterSummaries);
    // DocumentSummaryModel bookSummaryModel = new
    // DocumentSummaryModel(bookSummary);
    // model.setSummary(bookSummaryModel);
    // bookSummaryModel.setBook(model);

    // logger.info("Book summary generated and saved successfully for document: {}",
    // model.getFileName());
    // }

    private void generateAndSaveBookSummary(DocumentModel model) {
        logger.info("Generating and saving book summary for document: {}", model.getFileName());

        ProgressCallback progressCallback = (progress) -> {
            progressService.updateProgress(progress);
            logger.debug("Book summary progress: {}", progress);
        };

        summaryGenerationService.generateDocumentSummary(model, progressCallback);

        sqlDocumentService.saveDoc(model);
        logger.info("Book summary generated and saved successfully for document: {}", model.getFileName());
    }
}
