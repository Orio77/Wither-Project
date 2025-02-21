package com.Orio.wither_project.summary.service.generation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.summary.config.SummaryPromptConfig;
import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.model.DocumentSummaryModel;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.model.PageSummaryModel;
import com.Orio.wither_project.summary.service.generation.IPDFParallelSummaryGenerationService;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OllamaParallelSummaryGenerationService implements IPDFParallelSummaryGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(OllamaParallelSummaryGenerationService.class);

    private final OllamaChatModel ollamaChatModel;
    private final SummaryPromptConfig promptConfig;

    // Thread pool configuration
    private static final int BATCH_SIZE = 10;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int QUEUE_CAPACITY = 100;
    private static final int TIMEOUT_MINUTES = 30;

    private final ExecutorService executorService = new ThreadPoolExecutor(
            THREAD_POOL_SIZE,
            THREAD_POOL_SIZE,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @PreDestroy
    public void cleanup() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private String parallelSummarize(String text, String instruction) {
        if (text == null || text.trim().isEmpty()) {
            return "No text provided for summarization.";
        }

        try {
            List<String> chunks = splitTextIntoChunks(text, 2000); // Split into 2000-character chunks
            List<Future<String>> futures = new ArrayList<>();

            // Process each chunk in parallel
            for (String chunk : chunks) {
                futures.add(executorService.submit(() -> processSingleChunk(chunk, instruction)));
            }

            // Combine results
            StringBuilder combinedSummary = new StringBuilder();
            for (Future<String> future : futures) {
                combinedSummary.append(future.get(TIMEOUT_MINUTES, TimeUnit.MINUTES)).append(" ");
            }

            // If multiple chunks were processed, summarize the combined result
            if (chunks.size() > 1) {
                return processSingleChunk(combinedSummary.toString(), instruction);
            }

            return combinedSummary.toString().trim();
        } catch (Exception e) {
            logger.error("Failed to generate parallel summary: {}", e.getMessage(), e);
            return "Error during summary generation: " + e.getMessage();
        }
    }

    private String processSingleChunk(String text, String instruction) {
        UserMessage instructionMessage = new UserMessage(instruction);
        UserMessage userMessage = new UserMessage(text);
        Prompt prompt = new Prompt(
                List.of(promptConfig.getContinuousSummarySystemMessage(), instructionMessage, userMessage),
                OllamaOptions.builder().withFormat("json").build());

        ChatResponse response = ollamaChatModel.call(prompt);
        return response.getResult().getOutput().getContent();
    }

    private List<String> splitTextIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += chunkSize) {
            chunks.add(text.substring(i, Math.min(text.length(), i + chunkSize)));
        }
        return chunks;
    }

    @Override
    public List<PageSummaryModel> generatePageSummaries(List<PageModel> pages) {
        logger.info("Generating page summaries with {} threads in batches of {}", THREAD_POOL_SIZE, BATCH_SIZE);

        List<PageSummaryModel> results = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < pages.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, pages.size());
            List<PageModel> batch = pages.subList(i, end);

            Future<?> future = executorService.submit(() -> {
                for (PageModel page : batch) {
                    try {
                        String summaryText = parallelSummarize(page.getContent(), promptConfig.getPage());
                        PageSummaryModel summaryModel = new PageSummaryModel(summaryText);
                        summaryModel.setPage(page);
                        page.setSummary(summaryModel);
                        results.add(summaryModel);
                    } catch (Exception e) {
                        logger.error("Error processing page {}: {}", page.getPageNumber(), e.getMessage(), e);
                    }
                }
            });
            futures.add(future);
        }

        waitForCompletion(futures);
        return new ArrayList<>(results);
    }

    @Override
    public List<ChapterSummaryModel> generateChapterSummaries(List<ChapterModel> chapters) {
        logger.info("Generating chapter summaries using {} threads", THREAD_POOL_SIZE);

        List<ChapterSummaryModel> results = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> futures = new ArrayList<>();

        for (ChapterModel chapter : chapters) {
            Future<?> future = executorService.submit(() -> {
                try {
                    String text = chapter.getPages().stream()
                            .map(p -> p.getSummary().getContent())
                            .collect(Collectors.joining("\n\n\n\n"));
                    String summaryText = parallelSummarize(text, promptConfig.getChapter());
                    ChapterSummaryModel summaryModel = new ChapterSummaryModel(summaryText);
                    summaryModel.setChapter(chapter);
                    chapter.setSummary(summaryModel);
                    results.add(summaryModel);
                } catch (Exception e) {
                    logger.error("Error processing chapter {}: {}", chapter.getChapterNumber(), e.getMessage(), e);
                }
            });
            futures.add(future);
        }

        waitForCompletion(futures);
        return new ArrayList<>(results);
    }

    @Override
    public DocumentSummaryModel generateBookSummary(DocumentModel doc) {
        String fullText = doc.getChapters().stream()
                .map(ch -> ch.getSummary().getContent())
                .collect(Collectors.joining("\n\n\n\n"));
        String bookSummaryText = parallelSummarize(fullText, promptConfig.getBook());
        DocumentSummaryModel bookSummary = new DocumentSummaryModel(bookSummaryText);
        bookSummary.setBook(doc);
        doc.setSummary(bookSummary);
        return bookSummary;
    }

    private void waitForCompletion(List<Future<?>> futures) {
        try {
            for (Future<?> future : futures) {
                future.get(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            logger.error("Error waiting for task completion: {}", e.getMessage(), e);
            futures.forEach(f -> f.cancel(true));
            throw new RuntimeException("Failed to complete parallel processing", e);
        }
    }
}
