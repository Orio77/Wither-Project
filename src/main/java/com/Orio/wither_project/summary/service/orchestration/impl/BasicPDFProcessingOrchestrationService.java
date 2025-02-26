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
import com.Orio.wither_project.summary.model.ProcessingProgressModel;
import com.Orio.wither_project.summary.service.conversion.IPDFConversionService;
import com.Orio.wither_project.summary.service.extraction.IPDFContentExtractionService;
import com.Orio.wither_project.summary.service.extraction.IPDFMetaDataExtractionService;
import com.Orio.wither_project.summary.service.generation.IPDFSummaryGenerationService;
import com.Orio.wither_project.summary.service.orchestration.IPDFProcessingOrchestrationService;
import com.Orio.wither_project.summary.service.progress.ProcessingProgressService;
import com.Orio.wither_project.summary.service.storage.ISQLDocumentService;
import com.google.common.collect.Lists;

import com.Orio.wither_project.summary.exception.PDFConversionException;
import com.Orio.wither_project.summary.exception.PDFContentExtractionException;
import com.Orio.wither_project.summary.exception.PDFMetadataExtractionException;
import com.Orio.wither_project.summary.exception.PDFSummaryGenerationException;
import com.Orio.wither_project.summary.exception.PDFStorageException;

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
    private final ProcessingProgressService processingProgressService;

    @Override
    public boolean continueProcessingPDF(FileEntity file) throws IOException {
        Assert.notNull(file, "File entity cannot be null");
        // Consistently use file.getName() to identify documents
        String identifier = file.getName();

        ProcessingProgressModel progress = processingProgressService.getOrCreateProgress(identifier);
        logger.info("Continuing PDF processing for file: {}, current progress: {}", identifier, progress);

        try {
            // Check if we need to convert the document
            DocumentModel doc;
            if (!progress.isConversionCompleted()) {
                doc = convert(file);
                if (doc == null) {
                    logger.error("Failed to convert document: {}", identifier);
                    return false;
                }
                save(doc);
                progress.setConversionCompleted(true);
                processingProgressService.updateProgress(progress);
            } else {
                // Load existing document from database
                doc = sqlDocumentService.getDocument(identifier);
                if (doc == null) {
                    logger.error("Document marked as converted but not found in database: {}", identifier);
                    processingProgressService.resetProgress(identifier);
                    throw new PDFStorageException("Document not found in database: " + identifier);
                }
            }

            // Continue with remaining steps
            boolean success = true;

            if (!progress.isMetadataCompleted()) {
                try {
                    success = setMetadata(doc);
                    if (success) {
                        progress.setMetadataCompleted(true);
                        processingProgressService.updateProgress(progress);
                    } else {
                        logger.error("Failed to set metadata for document: {}", identifier);
                    }
                } catch (Exception e) {
                    logger.error("Exception setting metadata for document: {}", identifier, e);
                    success = false;
                }
            }

            if (success && !progress.isContentsCompleted()) {
                try {
                    success = setContents(doc);
                    if (success) {
                        progress.setContentsCompleted(true);
                        processingProgressService.updateProgress(progress);
                    } else {
                        logger.error("Failed to set contents for document: {}", identifier);
                    }
                } catch (Exception e) {
                    logger.error("Exception setting contents for document: {}", identifier, e);
                    success = false;
                }
            }

            if (success && !progress.isPageSummariesCompleted()) {
                try {
                    success = setPageSummaries(doc);
                    if (success) {
                        progress.setPageSummariesCompleted(true);
                        processingProgressService.updateProgress(progress);
                    } else {
                        logger.error("Failed to set page summaries for document: {}", identifier);
                    }
                } catch (Exception e) {
                    logger.error("Exception setting page summaries for document: {}", identifier, e);
                    success = false;
                }
            }

            if (success && !progress.isChapterSummariesCompleted()) {
                try {
                    success = setChapterSummaries(doc);
                    if (success) {
                        progress.setChapterSummariesCompleted(true);
                        processingProgressService.updateProgress(progress);
                    } else {
                        logger.error("Failed to set chapter summaries for document: {}", identifier);
                    }
                } catch (Exception e) {
                    logger.error("Exception setting chapter summaries for document: {}", identifier, e);
                    success = false;
                }
            }

            if (success && !progress.isDocumentSummaryCompleted()) {
                try {
                    success = setDocumentSummary(doc);
                    if (success) {
                        progress.setDocumentSummaryCompleted(true);
                        processingProgressService.updateProgress(progress);
                    } else {
                        logger.error("Failed to set document summary for document: {}", identifier);
                    }
                } catch (Exception e) {
                    logger.error("Exception setting document summary for document: {}", identifier, e);
                    success = false;
                }
            }

            return success;
        } catch (Exception e) {
            logger.error("Unhandled exception during PDF processing for file: {}", identifier, e);
            return false;
        }
    }

    @Override
    public DocumentModel convert(FileEntity file) throws PDFProcessingException {
        Assert.notNull(file, "File entity cannot be null");
        logger.info("Starting PDF conversion for file: {}", file.getName());

        try (PDDocument doc = conversionService.convertToPdDocument(file)) {
            DocumentModel docModel = new DocumentModel();

            try {
                List<ChapterModel> chapters = contentExtractionService.getChapters(doc);
                docModel.addChapters(chapters);
            } catch (Exception e) {
                throw new PDFContentExtractionException("Failed to extract chapters from PDF: " + file.getName(), e);
            }

            try {
                docModel.setAuthor(metaDataExtractionService.getAuthor(doc));
                docModel.setTitle(metaDataExtractionService.getTitle(doc));
                docModel.setFileName(metaDataExtractionService.getFileName(doc));
            } catch (Exception e) {
                throw new PDFMetadataExtractionException("Failed to extract metadata from PDF: " + file.getName(), e);
            }

            logger.info("PDF conversion completed for file: {}", file.getName());
            return docModel;
        } catch (PDFContentExtractionException | PDFMetadataExtractionException e) {
            throw e;
        } catch (IOException e) {
            throw new PDFConversionException("Failed to open or process PDF file: " + file.getName(), e);
        } catch (Exception e) {
            throw new PDFProcessingException("Unexpected error during PDF conversion: " + file.getName(), e);
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
    public boolean setSummaries(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.notNull(model.getChapters(), "Chapters cannot be null");

        logger.info("Setting summaries for document: {}", model.getFileName());
        try {
            // Breaking down into smaller steps for better resumability
            return setPageSummaries(model) &&
                    setChapterSummaries(model) &&
                    setDocumentSummary(model);
        } catch (Exception e) {
            logger.error("Error setting summaries for document: {}", model.getFileName(), e);
            throw new PDFProcessingException("Failed to set summaries", e);
        }
    }

    @Override
    public boolean setPageSummaries(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.notNull(model.getChapters(), "Chapters cannot be null");

        try {
            List<PageModel> pages = extractPages(model.getChapters());

            // Get progress to check where we left off
            // Use a consistent identifier - fileName from the model
            String identifier = model.getFileName();
            ProcessingProgressModel progress = processingProgressService.getOrCreateProgress(identifier);
            int startIndex = Math.max(0, progress.getLastProcessedPageIndex() + 1);

            if (startIndex >= pages.size()) {
                logger.info("All page summaries already processed for document: {}", identifier);
                return true;
            }

            logger.info("Generating page summaries starting from index {} of {} pages",
                    startIndex, pages.size());

            List<PageModel> remainingPages = pages.subList(startIndex, pages.size());
            try {
                generateAndSavePageSummaries(remainingPages, pages.size(), startIndex, identifier);
            } catch (PDFSummaryGenerationException e) {
                logger.error("Failed to generate page summaries: {}", e.getMessage());
                return false;
            }

            return true;
        } catch (Exception e) {
            logger.error("Error in page summary generation process for document: {}", model.getFileName(), e);
            throw new PDFProcessingException("Failed to set page summaries", e);
        }
    }

    @Override
    public boolean setChapterSummaries(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.notNull(model.getChapters(), "Chapters cannot be null");

        try {
            List<ChapterModel> chapters = model.getChapters();

            // Get progress to check where we left off
            // Use a consistent identifier - fileName from the model
            String identifier = model.getFileName();
            ProcessingProgressModel progress = processingProgressService.getOrCreateProgress(identifier);
            int startIndex = Math.max(0, progress.getLastProcessedChapterIndex() + 1);

            if (startIndex >= chapters.size()) {
                logger.info("All chapter summaries already processed for document: {}", identifier);
                return true;
            }

            logger.info("Generating chapter summaries starting from index {} of {} chapters",
                    startIndex, chapters.size());

            List<ChapterModel> remainingChapters = chapters.subList(startIndex, chapters.size());

            ProgressCallback progressCallback = (progressValue) -> {
                // Convert progress value from 0-1 to 0-100 for consistency
                progressService.updateProgress(progressValue);
                logger.debug("Chapter summary progress: {}", progressValue);
            };

            summaryGenerationService.generateChapterSummaries(remainingChapters, progressCallback);
            sqlDocumentService.saveChapters(remainingChapters);

            // Update the progress
            progress.setLastProcessedChapterIndex(chapters.size() - 1);
            processingProgressService.updateProgress(progress);

            logger.info("Chapter summaries generated and saved successfully");
            return true;
        } catch (Exception e) {
            logger.error("Error setting chapter summaries for document: {}", model.getFileName(), e);
            throw new PDFProcessingException("Failed to set chapter summaries", e);
        }
    }

    @Override
    public boolean setDocumentSummary(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.notNull(model.getChapters(), "Chapters cannot be null");

        try {
            // Get progress to check if we've already done this
            // Use a consistent identifier - fileName from the model
            String identifier = model.getFileName();
            ProcessingProgressModel progress = processingProgressService.getOrCreateProgress(identifier);

            if (progress.isDocumentSummaryCompleted()) {
                logger.info("Document summary already processed for document: {}", identifier);
                return true;
            }

            logger.info("Generating document summary for document: {}", identifier);

            ProgressCallback progressCallback = (progressValue) -> {
                // Progress value already between 0-1, no need to convert
                progressService.updateProgress(progressValue);
                logger.debug("Book summary progress: {}", progressValue);
            };

            summaryGenerationService.generateDocumentSummary(model, progressCallback);
            sqlDocumentService.saveDoc(model);

            logger.info("Book summary generated and saved successfully for document: {}", identifier);
            return true;
        } catch (Exception e) {
            logger.error("Error setting document summary for document: {}", model.getFileName(), e);
            throw new PDFProcessingException("Failed to set document summary", e);
        }
    }

    private List<PageModel> extractPages(List<ChapterModel> chapters) {
        return chapters.stream()
                .flatMap(chapter -> chapter.getPages().stream())
                .collect(Collectors.toList());
    }

    private void generateAndSavePageSummaries(List<PageModel> pages, int totalPages, int startIndex,
            String identifier) {
        logger.debug("Generating and saving page summaries");

        final int BATCH_SIZE = 5;
        AtomicInteger processedPages = new AtomicInteger(0);
        AtomicInteger failedPages = new AtomicInteger(0);

        List<List<PageModel>> batches = Lists.partition(pages, BATCH_SIZE);
        ProcessingProgressModel progress = processingProgressService.getOrCreateProgress(identifier);

        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = batches.stream()
                .map(batch -> CompletableFuture.runAsync(() -> {
                    try {
                        summaryGenerationService.generatePageSummaries(batch);

                        try {
                            sqlDocumentService.savePages(batch);
                        } catch (Exception e) {
                            logger.error("Failed to save page summaries batch: {}", e.getMessage(), e);
                            failedPages.addAndGet(batch.size());
                            throw new PDFStorageException("Failed to save page summaries", e);
                        }

                        int currentBatchSize = batch.size();
                        int currentProgress = processedPages.addAndGet(currentBatchSize);

                        // Update overall progress for UI - ensure progress value is between 0-1
                        double progressPercentage = (double) (startIndex + currentProgress) / totalPages;
                        progressService.updateProgress(progressPercentage);

                        // Update processing progress in database
                        int lastProcessedIndex = startIndex + currentProgress - 1;
                        synchronized (progress) {
                            if (lastProcessedIndex > progress.getLastProcessedPageIndex()) {
                                progress.setLastProcessedPageIndex(lastProcessedIndex);
                                processingProgressService.updateProgress(progress);
                            }
                        }

                        logger.debug("Processed batch of {} pages. Progress: {}/{} ({}%)",
                                currentBatchSize, startIndex + currentProgress, totalPages,
                                Math.round(progressPercentage * 100));
                    } catch (Exception e) {
                        logger.error("Error processing page summary batch: {}", e.getMessage(), e);
                        failedPages.addAndGet(batch.size());
                    }
                }, ForkJoinPool.commonPool()))
                .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(futures).get();
            logger.debug("Page summaries generated and saved successfully");

            if (failedPages.get() > 0) {
                logger.warn("Failed to process {} page summaries out of {}", failedPages.get(), pages.size());
                throw new PDFSummaryGenerationException("Failed to generate summaries for " +
                        failedPages.get() + " pages out of " + pages.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PDFSummaryGenerationException("Page summary generation interrupted", e);
        } catch (ExecutionException e) {
            throw new PDFSummaryGenerationException("Failed to generate page summaries", e.getCause());
        }
    }

    @Override
    public void save(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.notNull(model.getChapters(), "Chapters cannot be null");
        logger.info("Saving document: {}", model.getFileName());

        try {
            sqlDocumentService.saveDoc(model);
            logger.info("Document saved successfully: {}", model.getFileName());
        } catch (Exception e) {
            logger.error("Failed to save document: {}", model.getFileName(), e);
            throw new PDFStorageException("Failed to save document: " + model.getFileName(), e);
        }

        logger.info("Saving chapters for document: {}", model.getFileName());
        List<ChapterModel> chapters = model.getChapters();
        try {
            sqlDocumentService.saveChapters(chapters);
            logger.info("{} Chapters saved successfully for document: {}", chapters.size(), model.getFileName());
        } catch (Exception e) {
            logger.error("Failed to save chapters for document: {}", model.getFileName(), e);
            throw new PDFStorageException("Failed to save chapters for document: " + model.getFileName(), e);
        }

        AtomicInteger totalPageCount = new AtomicInteger(0);
        AtomicInteger failedChapters = new AtomicInteger(0);

        for (ChapterModel chapter : chapters) {
            try {
                logger.info("Saving pages for chapter: {}", chapter.getTitle());
                sqlDocumentService.savePages(chapter.getPages());
                totalPageCount.addAndGet(chapter.getPages().size());
                logger.info("{} Pages saved successfully for chapter: {}", chapter.getPages().size(),
                        chapter.getTitle());
            } catch (Exception e) {
                logger.error("Failed to save pages for chapter: {}", chapter.getTitle(), e);
                failedChapters.incrementAndGet();
            }
        }

        if (failedChapters.get() > 0) {
            logger.warn("Failed to save pages for {} chapters out of {}", failedChapters.get(), chapters.size());
            throw new PDFStorageException("Failed to save pages for " + failedChapters.get() +
                    " chapters out of " + chapters.size());
        }

        logger.info("{} Pages saved successfully for document: {}", totalPageCount.get(), model.getFileName());
    }
}
