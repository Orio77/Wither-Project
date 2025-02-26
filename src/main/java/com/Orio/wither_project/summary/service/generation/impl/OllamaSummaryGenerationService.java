package com.Orio.wither_project.summary.service.generation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.ResourceAccessException;

import com.Orio.wither_project.socket.summary.model.ProgressCallback;
import com.Orio.wither_project.summary.config.SummaryConstantsConfig;
import com.Orio.wither_project.summary.config.SummaryPromptConfig;
import com.Orio.wither_project.summary.exception.InvalidResponseException;
import com.Orio.wither_project.summary.exception.ModelNotAvailableException;
import com.Orio.wither_project.summary.exception.SummaryParsingException;
import com.Orio.wither_project.summary.model.ChapterModel;
import com.Orio.wither_project.summary.model.ChapterSummaryModel;
import com.Orio.wither_project.summary.model.DocumentModel;
import com.Orio.wither_project.summary.model.DocumentSummaryModel;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.model.PageSummaryModel;
import com.Orio.wither_project.summary.model.SummaryResponse;
import com.Orio.wither_project.summary.model.SummaryType;
import com.Orio.wither_project.summary.service.generation.IPDFSummaryGenerationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OllamaSummaryGenerationService implements IPDFSummaryGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(OllamaSummaryGenerationService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final OllamaChatModel ollamaChatModel;
    private final SummaryPromptConfig promptConfig;
    private final SummaryConstantsConfig constantsConfig;
    private final ObjectMapper objectMapper;

    @Override
    @Retryable(value = { ResourceAccessException.class,
            ModelNotAvailableException.class }, maxAttempts = MAX_RETRY_ATTEMPTS, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String summarize(String text, SummaryType type) {
        logger.info("Starting summary generation for type: {}", type);
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Received empty or null text for summarization");
            return "No text provided for summarization.";
        }
        String instruction = getInstructionForType(type);
        logger.debug("Using instruction for type {}: {}", type, instruction);
        return summarize(text, instruction);
    }

    @Recover
    public String summarizeFallback(Exception e, String text, SummaryType type) {
        logger.error("All retry attempts failed for summarization: {}", e.getMessage(), e);
        if (text != null && text.length() > 100) {
            // Return a truncated version of the original text as a fallback
            return "Summary generation failed. Original text (truncated): " +
                    text.substring(0, 100) + "...";
        }
        return "Summary generation failed: " + e.getMessage();
    }

    /**
     * Generate summary for text with specific instruction
     * 
     * @throws RuntimeException if summary generation fails
     */
    @Override
    @Retryable(value = { ResourceAccessException.class,
            ModelNotAvailableException.class }, maxAttempts = MAX_RETRY_ATTEMPTS, backoff = @Backoff(delay = 1000, multiplier = 2))
    public String summarize(String text, String instruction) {
        logger.info("Starting text summarization with custom instruction");
        logger.debug("Text length: {} characters", text.length());
        logger.debug("Instruction: {}", instruction);

        try {
            UserMessage instructionMessage = new UserMessage(instruction);
            UserMessage userMessage = new UserMessage(text);
            Prompt prompt = new Prompt(
                    List.of(promptConfig.getDetailedTechnicalSummarySystemMessage(),
                            promptConfig.getContinuousSummarySystemMessage(),
                            promptConfig.getSummaryJsonSchema(),
                            instructionMessage, userMessage),
                    OllamaOptions.builder().withFormat("json").build());

            logger.debug("Sending request to Ollama model");
            ChatResponse response = ollamaChatModel.call(prompt);

            if (response == null) {
                logger.error("Received null response from Ollama model");
                throw new ModelNotAvailableException("Ollama model returned null response");
            }

            if (response.getResult() == null) {
                logger.error("Ollama model result is null");
                throw new InvalidResponseException("Ollama model returned null result");
            }

            String jsonResponse = response.getResult().getOutput().getContent();
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                logger.error("Ollama model returned empty content");
                throw new InvalidResponseException("Ollama model returned empty content");
            }

            logger.debug("Received JSON response: {}", jsonResponse);

            try {
                SummaryResponse summaryResponse = objectMapper.readValue(jsonResponse, SummaryResponse.class);
                if (summaryResponse == null || summaryResponse.getSummary() == null) {
                    throw new SummaryParsingException("Failed to parse summary from JSON response: null value");
                }

                String summary = summaryResponse.getSummary();
                logger.info("Summary extracted successfully. Length: {} characters", summary.length());
                return summary;
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse JSON response: {}", e.getMessage());
                throw new SummaryParsingException("Failed to parse summary from model response: " + jsonResponse, e);
            }
        } catch (ModelNotAvailableException | InvalidResponseException | SummaryParsingException e) {
            // Let these be caught by @Retryable
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during summary generation: {}", e.getMessage(), e);
            throw new RuntimeException("Error during summary generation", e);
        }
    }

    @Recover
    public String summarizeFallback(Exception e, String text, String instruction) {
        logger.error("All retry attempts failed for custom summarization: {}", e.getMessage(), e);
        if (text != null && text.length() > 100) {
            return "Summary generation failed. Original text (truncated): " +
                    text.substring(0, 100) + "...";
        }
        return "Summary generation failed: " + e.getMessage();
    }

    private String getInstructionForType(SummaryType type) {
        String instruction = switch (type) {
            case PAGE -> promptConfig.getPage();
            case CHAPTER -> promptConfig.getChapter();
            case BOOK -> promptConfig.getBook();
            default -> promptConfig.getDefaultPrompt();
        };
        logger.debug("Selected instruction for type {}: {}", type, instruction);
        return instruction;
    }

    @Override
    public String summarizeProgressively(String text, SummaryType type) {
        logger.info("Starting progressive summarization for type: {}", type);
        logger.debug("Input text length: {} characters", text != null ? text.length() : 0);

        if (text == null || text.trim().isEmpty()) {
            logger.warn("Received empty or null text for progressive summarization");
            return "No text provided for summarization.";
        }

        try {
            int groupSize = determineGroupSize(type);
            logger.debug("Determined group size: {} for type: {}", groupSize, type);

            List<String> parts = splitTextIntoParts(text, groupSize);
            logger.debug("Split text into {} parts", parts.size());

            return processProgressiveSummarization(parts, type, groupSize);
        } catch (Exception e) {
            logger.error("Error during progressive summarization: {}", e.getMessage(), e);
            return "Error during progressive summarization: " + e.getMessage();
        }
    }

    private List<String> splitTextIntoParts(String text, int groupSize) {
        String[] parts = text.split(constantsConfig.getSplitRegex());
        logger.debug("Initial split resulted in {} raw parts", parts.length);

        if (parts.length == 0) {
            logger.warn("Text splitting resulted in zero parts");
            throw new IllegalArgumentException("Text could not be split into parts");
        }

        List<String> groups = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i % groupSize == 0 && sb.length() > 0) {
                groups.add(sb.toString());
                logger.debug("Created group {} with {} characters", groups.size(), sb.length());
                sb = new StringBuilder();
            }
            sb.append("\n").append(parts[i]);
        }

        if (sb.length() > 0) {
            groups.add(sb.toString());
            logger.debug("Added final group {} with {} characters", groups.size(), sb.length());
        }

        logger.debug("Text splitting completed with {} groups", groups.size());
        return groups;
    }

    private int determineGroupSize(SummaryType type) {
        int size = switch (type) {
            case CHAPTER -> 5;
            case BOOK -> 3;
            default -> 2;
        };
        logger.debug("Determined group size {} for summary type {}", size, type);
        return size;
    }

    private String processProgressiveSummarization(List<String> groups, SummaryType type, int groupSize) {
        long startTime = System.currentTimeMillis();

        String initialSummary = getInitialSummary(groups.get(0), type);
        groups.remove(0);

        if (groups.size() == 1) {
            logger.debug("Single part detected, returning initial summary of {} characters", initialSummary.length());
            return initialSummary;
        }

        logger.debug("Processing {} additional groups for summarization", groups.size());
        String finalSummary = getCompleteSummary(initialSummary, groups, type);

        logger.debug("Progressive summarization completed in {}ms. Final length: {} characters",
                System.currentTimeMillis() - startTime, finalSummary.length());
        return finalSummary;
    }

    private String getInitialSummary(String firstPart, SummaryType type) {
        logger.debug("Generating initial summary. Input length: {} characters, type: {}", firstPart.length(), type);

        String instruction = switch (type) {
            case CHAPTER -> promptConfig.getChapterInitial();
            case BOOK -> promptConfig.getBookInitial();
            default -> promptConfig.getDefaultInitial();
        };
        logger.debug("Using initial instruction: {}", instruction);

        String summary = summarize(firstPart, instruction);
        logger.debug("Generated initial summary of {} characters", summary.length());
        return summary;
    }

    private String getCompleteSummary(String initialSummary, List<String> groups, SummaryType type) {
        long startTime = System.currentTimeMillis();
        int size = groups.size();
        logger.debug("Building complete summary from {} parts. Initial summary: {} characters",
                size, initialSummary.length());

        StringBuilder currentSummary = new StringBuilder(initialSummary);

        for (int i = 0; i < size; i++) {
            logger.debug("Processing part {}/{} - Input length: {} characters",
                    i + 1, size, groups.get(i).length());

            String instruction = switch (type) {
                case CHAPTER -> String.format(promptConfig.getChapterExpand(), currentSummary);
                case BOOK -> String.format(promptConfig.getBookExpand(), currentSummary);
                default -> String.format(promptConfig.getDefaultExpand(), currentSummary);
            };

            String newSummary = summarize(groups.get(i), instruction);
            currentSummary.append("\n\n").append(newSummary);
            logger.debug("Added summary part {}: {} characters", i + 1, newSummary.length());
        }

        logger.debug("Complete summary built in {}ms. Total length: {} characters",
                System.currentTimeMillis() - startTime, currentSummary.length());
        return currentSummary.toString();
    }

    @Override
    public List<PageSummaryModel> generatePageSummaries(List<PageModel> pages) {
        logger.info("Generating page summaries sequentially for {} pages", pages.size());
        List<PageSummaryModel> summaries = new ArrayList<>();
        List<Integer> failedPages = new ArrayList<>();

        for (int j = 0; j < pages.size(); j++) {
            PageModel page = pages.get(j);
            try {
                String text = (page.getContent().trim().isEmpty()) ? "No content for this page" : page.getContent();
                String summaryText;

                if (text.equals("No content for this page")) {
                    summaryText = text;
                } else {
                    try {
                        summaryText = summarize(text, SummaryType.PAGE);
                    } catch (Exception e) {
                        logger.error("Failed to generate summary for page {}: {}", j, e.getMessage());
                        failedPages.add(j);
                        summaryText = "Summary generation failed: " + e.getMessage();
                    }
                }

                PageSummaryModel summaryModel = new PageSummaryModel(summaryText);
                summaryModel.addPage(page);
                summaries.add(summaryModel);
            } catch (Exception e) {
                logger.error("Critical error processing page {}: {}", j, e.getMessage(), e);
                failedPages.add(j);
                // Create an error summary to preserve the processing flow
                PageSummaryModel errorSummary = new PageSummaryModel("Error: " + e.getMessage());
                errorSummary.addPage(page);
                summaries.add(errorSummary);
            }
        }

        if (!failedPages.isEmpty()) {
            logger.warn("Failed to generate summaries for {} pages: {}",
                    failedPages.size(), failedPages);
        }

        return summaries;
    }

    @Override
    public List<ChapterSummaryModel> generateChapterSummaries(List<ChapterModel> chapters,
            ProgressCallback progressCallback) {
        Assert.notNull(chapters, "Chapters list cannot be null");
        Assert.notNull(progressCallback, "Progress callback cannot be null");
        logger.info("Generating chapter summaries progressively for {} chapters", chapters.size());
        List<ChapterSummaryModel> summaries = new ArrayList<>();

        for (ChapterModel chapter : chapters) {
            logger.debug("Processing chapter: {}", chapter.getTitle());
            String text = collectSummaries(chapter.getPages(), PageModel::getSummary,
                    summary -> summary.getContent());

            if (text.trim().isEmpty()) {
                summaries.add(createEmptySummary(chapter, "No content for this chapter",
                        ChapterSummaryModel::new, ChapterSummaryModel::addChapter));
                continue;
            }

            try {
                String summary = generateProgressiveSummary(text, SummaryType.CHAPTER, progressCallback);
                ChapterSummaryModel summaryModel = new ChapterSummaryModel(summary);
                summaryModel.addChapter(chapter);
                summaries.add(summaryModel);
            } catch (Exception e) {
                logger.error("Error processing chapter summary: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to generate chapter summary", e);
            }
        }

        return summaries;
    }

    @Override
    public DocumentSummaryModel generateDocumentSummary(DocumentModel model, ProgressCallback progressCallback) {
        Assert.notNull(model, "Document model cannot be null");
        Assert.notNull(progressCallback, "Progress callback cannot be null");
        logger.info("Generating document summary for document: {}", model.getTitle());

        String chapterSummaries = collectSummaries(model.getChapters(), ChapterModel::getSummary,
                summary -> summary.getContent());

        if (chapterSummaries.trim().isEmpty()) {
            return createEmptySummary(model, "No content for this document",
                    DocumentSummaryModel::new, DocumentSummaryModel::addDocument);
        }

        try {
            String summary = generateProgressiveSummary(chapterSummaries, SummaryType.BOOK, progressCallback);
            DocumentSummaryModel summaryModel = new DocumentSummaryModel(summary);
            summaryModel.addDocument(model);
            return summaryModel;
        } catch (Exception e) {
            logger.error("Error processing document summary: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate document summary", e);
        }
    }

    // Generic methods to eliminate redundancy

    /**
     * Generic method to collect summaries from a list of models
     */
    private <T, S> String collectSummaries(List<T> items, java.util.function.Function<T, S> getSummary,
            java.util.function.Function<S, String> getContent) {
        return items.stream()
                .map(getSummary)
                .filter(summary -> summary != null)
                .map(getContent)
                .collect(Collectors.joining(constantsConfig.getSplitRegex()));
    }

    /**
     * Generic method to create an empty summary
     */
    private <T, S> S createEmptySummary(T model, String message,
            java.util.function.Function<String, S> constructor,
            java.util.function.BiConsumer<S, T> addModel) {
        S emptySummary = constructor.apply(message);
        addModel.accept(emptySummary, model);
        return emptySummary;
    }

    /**
     * Generate progressive summary with progress tracking and error handling
     */
    private String generateProgressiveSummary(String text, SummaryType type, ProgressCallback progressCallback) {
        logger.debug("Starting progressive {} summary generation", type);
        List<String> parts = splitTextIntoParts(text, determineGroupSize(type));
        int totalParts = parts.size();
        AtomicInteger processedParts = new AtomicInteger(0);
        AtomicInteger failedParts = new AtomicInteger(0);

        if (parts.isEmpty()) {
            return "No content available for summarization";
        }

        // Process first part
        String initialSummary;
        try {
            initialSummary = getInitialSummary(parts.get(0), type);
            updateProgress(processedParts.incrementAndGet(), totalParts, progressCallback);
        } catch (Exception e) {
            logger.error("Failed to generate initial summary: {}", e.getMessage(), e);
            failedParts.incrementAndGet();
            initialSummary = "Failed to generate initial summary: " + e.getMessage() + "\n\nOriginal text (truncated): "
                    +
                    (parts.get(0).length() > 100 ? parts.get(0).substring(0, 100) + "..." : parts.get(0));
        }
        parts.remove(0);

        if (parts.isEmpty()) {
            return initialSummary;
        }

        // Process remaining parts
        StringBuilder currentSummary = new StringBuilder(initialSummary);
        for (String part : parts) {
            try {
                String instruction = switch (type) {
                    case CHAPTER -> String.format(promptConfig.getChapterExpand(), currentSummary);
                    case BOOK -> String.format(promptConfig.getBookExpand(), currentSummary);
                    default -> String.format(promptConfig.getDefaultExpand(), currentSummary);
                };

                String newSummary = summarize(part, instruction);
                currentSummary.append("\n\n").append(newSummary);
            } catch (Exception e) {
                logger.error("Failed to generate summary part: {}", e.getMessage(), e);
                failedParts.incrementAndGet();
                currentSummary.append("\n\n[Failed to generate summary: ")
                        .append(e.getMessage())
                        .append("]");
            } finally {
                updateProgress(processedParts.incrementAndGet(), totalParts, progressCallback);
            }
        }

        if (failedParts.get() > 0) {
            logger.warn("Failed to generate {} out of {} summary parts",
                    failedParts.get(), totalParts);
            currentSummary.append("\n\n[Note: ")
                    .append(failedParts.get())
                    .append(" out of ")
                    .append(totalParts)
                    .append(" summary parts failed to generate.]");
        }

        return currentSummary.toString();
    }

    private void updateProgress(int current, int total, ProgressCallback progressCallback) {
        if (progressCallback != null) {
            // Ensure progress value is consistently between 0 and 1
            double progress = (double) current / total;
            // Make sure progress stays within valid range
            progress = Math.max(0.0, Math.min(1.0, progress));
            progressCallback.onProgress(progress);
            logger.debug("Updated progress: {}/{} ({}%)", current, total, Math.round(progress * 100));
        }
    }
}
