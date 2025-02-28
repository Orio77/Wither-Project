package com.Orio.wither_project.summary.service.orchestration.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.Orio.wither_project.pdf.model.entity.FileEntity;
import com.Orio.wither_project.pdf.service.storage.ISQLPDFService;
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

    // Configuration parameters that could be externalized
    private static final int BATCH_SIZE = 5;
    private static final int TERMINATION_TIMEOUT_SECONDS = 30;

    // Average text size estimations for progressive summarization calculations
    private static final int AVERAGE_CHAPTER_SUMMARY_SIZE = 3000; // characters
    private static final int CHAPTER_GROUP_SIZE = 5;
    private static final int BOOK_GROUP_SIZE = 3;

    private final ISQLDocumentService sqlDocumentService;
    private final ISQLPDFService sqlPDFService;
    private final IPDFMetaDataExtractionService metaDataExtractionService;
    private final IPDFSummaryGenerationService summaryGenerationService;
    private final IPDFContentExtractionService contentExtractionService;
    private final IPDFConversionService conversionService;
    private final SummaryProgressService progressService;
    private DocumentModel doc;

    // Track total operations and progress
    private int totalOperations;
    private AtomicInteger completedOperations = new AtomicInteger(0);

    // Track operations by type
    private int pageOperations;
    private int chapterOperations;
    private int documentOperations;

    private static class ProcessingCheckpoints {
        boolean metadataRequired = true;
        boolean contentRequired = true;
        boolean pageSummariesRequired = true;
        boolean chapterSummariesRequired = true;
        boolean documentSummaryRequired = true;
    }

    private void init(FileEntity file) {
        DocumentModel storedModel = sqlDocumentService.getDocument(file.getName());
        this.doc = (storedModel == null) ? convert(file) : storedModel;
    }

    /**
     * Determines which processing steps are needed based on existing data
     * 
     * @return ProcessingCheckpoints object with flags for required steps
     */
    private ProcessingCheckpoints determineRequiredProcessing() {
        ProcessingCheckpoints checkpoints = new ProcessingCheckpoints();

        // Check if metadata exists
        checkpoints.metadataRequired = doc == null ||
                doc.getAuthor() == null ||
                doc.getTitle() == null;

        // Check if content exists
        checkpoints.contentRequired = doc == null ||
                doc.getChapters() == null ||
                doc.getChapters().isEmpty() ||
                doc.getChapters().stream().anyMatch(ch -> ch.getPages() == null ||
                        ch.getPages().isEmpty() ||
                        ch.getPages().stream().anyMatch(p -> p == null));

        // Check if summaries exist
        checkpoints.pageSummariesRequired = doc == null ||
                doc.getChapters() == null ||
                doc.getChapters().stream().anyMatch(ch -> ch.getPages() == null ||
                        ch.getPages().stream().anyMatch(p -> p.getSummary() == null));

        checkpoints.chapterSummariesRequired = doc == null ||
                doc.getChapters() == null ||
                doc.getChapters().stream().anyMatch(ch -> ch.getSummary() == null);

        checkpoints.documentSummaryRequired = doc == null ||
                doc.getSummary() == null;

        return checkpoints;
    }

    @Override
    public boolean processPDF(FileEntity file) throws IOException {
        init(file);

        // Determine what processing is needed
        ProcessingCheckpoints checkpoints = determineRequiredProcessing();

        boolean result = true;

        // Only perform steps that are needed
        if (checkpoints.metadataRequired) {
            result &= setMetadata(doc);
        }

        if (checkpoints.contentRequired) {
            result &= setContents(doc);
        }

        // Save at this point to ensure document structure exists
        save(doc);

        // Only generate summaries if needed
        if (checkpoints.pageSummariesRequired ||
                checkpoints.chapterSummariesRequired ||
                checkpoints.documentSummaryRequired) {
            result &= setSummaries(doc);
        }

        return result;
    }

    @Override
    public DocumentModel convert(FileEntity file) throws PDFProcessingException {
        Assert.notNull(file, "File entity cannot be null");
        logger.info("Starting PDF conversion for file: {}", file.getName());

        try {
            // Initialize a properly populated document model
            DocumentModel model = new DocumentModel();
            model.setFileName(file.getFileName());

            // If using title from file name, we can set it here
            String title = file.getName();
            if (title != null) {
                model.setTitle(title);
            }

            logger.info("Created document model with fileName: {}, title: {}",
                    model.getFileName(), model.getTitle());

            return model;
        } catch (Exception e) {
            logger.error("Failed to convert file to document model: {}", e.getMessage());
            throw new PDFProcessingException("Failed to convert file", e);
        }
    }

    @Override
    public boolean setMetadata(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.hasText(model.getFileName(), "File name cannot be empty");
        logger.info("Setting metadata for document: {}", model.getFileName());

        try (PDDocument doc = loadPDDocument(model.getFileName())) {
            if (doc == null) {
                logger.error("Could not load PDF document for: {}", model.getFileName());
                return false;
            }

            model.setAuthor(metaDataExtractionService.getAuthor(doc));
            model.setTitle(metaDataExtractionService.getTitle(doc));

            return true;
        } catch (IOException e) {
            logger.error("Failed to set metadata for document: {}", model.getFileName(), e);
            return false;
        }
    }

    @Override
    public boolean setContents(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.hasText(model.getFileName(), "File name cannot be empty");
        logger.info("Setting contents for document: {}", model.getFileName());

        try (PDDocument doc = loadPDDocument(model.getFileName())) {
            if (doc == null) {
                logger.error("Could not load PDF document for: {}", model.getFileName());
                return false;
            }

            List<ChapterModel> chapters = contentExtractionService.getChapters(doc);
            model.addChapters(chapters);
            return true;
        } catch (IOException e) {
            logger.error("Failed to set contents for document: {}", model.getFileName(), e);
            return false;
        }
    }

    private PDDocument loadPDDocument(String fileName) throws IOException {
        FileEntity fileEntity = retrieveFileEntityByFileName(fileName);
        if (fileEntity == null) {
            logger.error("FileEntity not found for fileName: {}", fileName);
            return null;
        }
        return conversionService.convertToPdDocument(fileEntity);
    }

    private FileEntity retrieveFileEntityByFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("fileName is null or empty");
            return null;
        }

        FileEntity entity = sqlPDFService.getPDFByFileName(fileName);
        if (entity == null) {
            logger.error("No file entity found for fileName: {}", fileName);
        }
        return entity;
    }

    @Override
    public void save(DocumentModel model) {
        Assert.notNull(model, "Document model cannot be null");
        logger.info("Saving document: {}", model.getFileName());

        saveDocument(model);
        saveChaptersWithPages(model.getChapters(), model.getFileName());
    }

    private void saveDocument(DocumentModel model) {
        logger.info("Saving document metadata: {}", model.getFileName());
        sqlDocumentService.saveDoc(model);
        logger.info("Document saved successfully: {}", model.getFileName());
    }

    private void saveChaptersWithPages(List<ChapterModel> chapters, String documentName) {
        if (chapters == null || chapters.isEmpty()) {
            logger.info("No chapters to save for document: {}", documentName);
            return;
        }

        logger.info("Saving {} chapters for document: {}", chapters.size(), documentName);
        sqlDocumentService.saveChapters(chapters);

        AtomicInteger totalPageCount = new AtomicInteger(0);

        chapters.forEach(chapter -> {
            saveChapterPages(chapter, totalPageCount);
        });

        logger.info("{} Pages saved successfully for document: {}", totalPageCount.get(), documentName);
    }

    private void saveChapterPages(ChapterModel chapter, AtomicInteger pageCounter) {
        List<PageModel> pages = chapter.getPages();
        if (pages == null || pages.isEmpty()) {
            return;
        }

        logger.info("Saving pages for chapter: {}", chapter.getTitle());
        sqlDocumentService.savePages(pages);
        pageCounter.addAndGet(pages.size());
        logger.info("{} Pages saved successfully for chapter: {}", pages.size(), chapter.getTitle());
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

            List<ChapterModel> unprocessedChaptersByDocument = sqlDocumentService
                    .getUnprocessedChaptersByDocument(model.getTitle());

            if (unprocessedChaptersByDocument == null || unprocessedChaptersByDocument.isEmpty()) {
                logger.info("All chapters already have summaries set for document: {}", model.getFileName());
                // If everything is already processed, set progress to 100%
                model.setSummaryCompletionPercentage(100.0);
                sqlDocumentService.saveDoc(model);
                return true;
            }

            // Reset and calculate total operations
            resetProgressTracking();
            calculateTotalOperations(unprocessedChaptersByDocument, model);

            // Initialize progress
            model.setSummaryCompletionPercentage(0.0);
            sqlDocumentService.saveDoc(model);

            List<PageModel> allPages = extractPages(unprocessedChaptersByDocument);
            generateAndSavePageSummaries(allPages, model);
            generateAndSaveChapterSummaries(unprocessedChaptersByDocument, model);
            generateAndSaveBookSummary(model);

            // Ensure we set completion to 100% when everything is done
            model.setSummaryCompletionPercentage(100.0);
            sqlDocumentService.saveDoc(model);

            logger.info("Summaries set successfully for document: {}", model.getFileName());
            return true;
        } catch (Exception e) {
            logger.error("Error setting summaries for document: {}", model.getFileName(), e);
            throw new PDFProcessingException("Failed to set summaries", e);
        }
    }

    private void resetProgressTracking() {
        totalOperations = 0;
        completedOperations.set(0);
        pageOperations = 0;
        chapterOperations = 0;
        documentOperations = 0;
    }

    private void calculateTotalOperations(List<ChapterModel> chapters, DocumentModel model) {
        // Calculate page summary operations
        int pageCount = chapters.stream()
                .mapToInt(chapter -> chapter.getPages() != null ? chapter.getPages().size() : 0)
                .sum();
        this.pageOperations = pageCount;

        // Estimate chapter summary operations - each chapter requires progressive
        // summarization
        int totalChapterParts = 0;
        for (ChapterModel chapter : chapters) {
            // For each chapter, estimate number of parts based on its pages
            int pageTextLength = chapter.getPages().size() * 1000; // Rough estimate of text length
            int parts = calculateProgressiveParts(pageTextLength, CHAPTER_GROUP_SIZE);
            totalChapterParts += parts;
        }
        this.chapterOperations = totalChapterParts;

        // Estimate document summary operations
        int estimatedChapterSummariesLength = chapters.size() * AVERAGE_CHAPTER_SUMMARY_SIZE;
        this.documentOperations = calculateProgressiveParts(estimatedChapterSummariesLength, BOOK_GROUP_SIZE);

        // Total operations
        this.totalOperations = pageOperations + chapterOperations + documentOperations;

        logger.info("Calculated total operations: {} (pages: {}, chapters: {}, document: {})",
                totalOperations, pageOperations, chapterOperations, documentOperations);
    }

    private int calculateProgressiveParts(int textLength, int groupSize) {
        // Estimate how many parts the text will be split into for progressive
        // summarization
        // Add 1 for the initial summary
        return Math.max(1, (int) Math.ceil((double) textLength / (1000 * groupSize))) + 1;
    }

    private List<PageModel> extractPages(List<ChapterModel> chapters) {
        return chapters.stream()
                .flatMap(chapter -> chapter.getPages().stream())
                .collect(Collectors.toList());
    }

    private void generateAndSavePageSummaries(List<PageModel> pages, DocumentModel model) {
        logger.debug("Generating and saving page summaries");

        final int totalPages = pages.size();
        ExecutorService executorService = createThreadPool();

        try {
            processPageBatches(pages, totalPages, executorService, model);
            logger.debug("Page summaries generated and saved successfully");
        } catch (Exception e) {
            throw new PDFProcessingException("Failed to generate page summaries", e);
        } finally {
            shutdownThreadPool(executorService);
        }
    }

    private ExecutorService createThreadPool() {
        int processors = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.max(2, Math.min(processors - 1, 4));
        return Executors.newFixedThreadPool(poolSize);
    }

    private void processPageBatches(List<PageModel> pages, int totalPages,
            ExecutorService executorService, DocumentModel model) {
        List<List<PageModel>> batches = Lists.partition(pages, BATCH_SIZE);

        for (List<PageModel> batch : batches) {
            List<CompletableFuture<Void>> batchFutures = new ArrayList<>();

            for (PageModel page : batch) {
                CompletableFuture<Void> pageFuture = CompletableFuture.supplyAsync(() -> {
                    summaryGenerationService.generatePageSummary(page);
                    return page;
                }, executorService).thenAccept(processedPage -> {
                    // Save and update progress for each page individually
                    sqlDocumentService.savePage(processedPage);
                    updateProgressForOperation(1, model);
                });

                batchFutures.add(pageFuture);
            }

            // Wait for the entire batch to complete before proceeding to the next batch
            CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();
        }
    }

    // The saveBatchWithProgress method is no longer needed since we're handling
    // each page individually

    private void saveBatchWithProgress(List<PageModel> batch, DocumentModel model) {
        batch.forEach(page -> {
            sqlDocumentService.savePage(page);
            updateProgressForOperation(1, model); // Each page is 1 operation
        });
    }

    private void updateProgressForOperation(int operationsCompleted, DocumentModel model) {
        int current = completedOperations.addAndGet(operationsCompleted);
        double progress = (double) current / totalOperations;

        // Update socket progress
        progressService.updateProgress(progress);

        // Update model completion percentage
        model.setSummaryCompletionPercentage(progress * 100);
        sqlDocumentService.saveDoc(model);

        logger.debug("Progress: {}/{} operations completed ({}%). Document completion: {}%",
                current, totalOperations, String.format("%.2f", progress * 100),
                String.format("%.2f", model.getSummaryCompletionPercentage()));
    }

    private void shutdownThreadPool(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    private void generateAndSaveChapterSummaries(List<ChapterModel> chapters, DocumentModel model) {
        logger.info("Generating and saving chapter summaries");

        ProgressCallback progressCallback = (progress) -> {
            // Calculate actual operations completed
            int estimatedOperationsCompleted = (int) Math.round(progress * chapterOperations);

            // Only update if there's been meaningful progress
            if (estimatedOperationsCompleted > 0) {
                updateProgressForOperation(estimatedOperationsCompleted, model);
            }
        };

        summaryGenerationService.generateChapterSummaries(chapters, progressCallback);

        sqlDocumentService.saveChapters(chapters);
        logger.info("Chapter summaries generated and saved successfully");

        // Ensure we mark all chapter operations as complete
        int remaining = chapterOperations - (completedOperations.get() - pageOperations);
        if (remaining > 0) {
            updateProgressForOperation(remaining, model);
        }
    }

    private void generateAndSaveBookSummary(DocumentModel model) {
        logger.info("Generating and saving book summary for document: {}", model.getFileName());

        ProgressCallback progressCallback = (progress) -> {
            // Calculate actual operations completed
            int estimatedOperationsCompleted = (int) Math.round(progress * documentOperations);

            // Only update if there's been meaningful progress
            if (estimatedOperationsCompleted > 0) {
                updateProgressForOperation(estimatedOperationsCompleted, model);
            }
        };

        summaryGenerationService.generateDocumentSummary(model, progressCallback);

        sqlDocumentService.saveDoc(model);
        logger.info("Book summary generated and saved successfully for document: {}", model.getFileName());

        // Ensure we mark all document operations as complete
        int remaining = documentOperations - (completedOperations.get() - pageOperations - chapterOperations);
        if (remaining > 0) {
            updateProgressForOperation(remaining, model);
        }
    }
}
