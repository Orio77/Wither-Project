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
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.Orio.wither_project.socket.summary.model.ProgressCallback;
import com.Orio.wither_project.summary.config.SummaryConstantsConfig;
import com.Orio.wither_project.summary.config.SummaryPromptConfig;
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

    private final OllamaChatModel ollamaChatModel;
    private final SummaryPromptConfig promptConfig;
    private final SummaryConstantsConfig constantsConfig;
    private final ObjectMapper objectMapper;

    @Override
    public String summarize(String text, SummaryType type) {
        logger.info("Starting summary generation for type: {}", type);
        if (text == null || text.trim().isEmpty()) {
            logger.warn("Received empty or null text for summarization");
            return null;
        }
        String instruction = getInstructionForType(type);
        logger.debug("Using instruction for type {}: {}", type, instruction);
        return summarize(text, instruction);
    }

    @Override
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

            if (response != null && response.getResult() != null) {
                String jsonResponse = response.getResult().getOutput().getContent();
                logger.debug("Received JSON response: {}", jsonResponse);

                try {
                    SummaryResponse summaryResponse = objectMapper.readValue(jsonResponse, SummaryResponse.class);
                    String summary = summaryResponse.getSummary();
                    logger.info("Summary extracted successfully. Length: {} characters", summary.length());
                    return summary;
                } catch (JsonProcessingException e) {
                    logger.error("Failed to parse JSON response: {}", e.getMessage());
                    return null;
                }
            } else {
                logger.warn("Received null response or result from Ollama model");
                return null;
            }

        } catch (Exception e) {
            logger.error("Failed to generate summary: {}", e.getMessage(), e);
            return null;
        }
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

        String summary = null;
        int attempts = 0;

        while (summary == null || summary.trim().isEmpty()) {
            attempts++;
            logger.debug("Initial summary generation attempt #{}", attempts);

            try {
                summary = summarize(firstPart, instruction);

                if (summary == null || summary.trim().isEmpty()) {
                    logger.warn("Initial summary generation attempt #{} failed: empty or null summary", attempts);
                    Thread.sleep(1000); // Wait 1 second between attempts
                } else {
                    logger.info("Successfully generated initial summary on attempt #{}. Length: {} characters",
                            attempts, summary.length());
                }
            } catch (Exception e) {
                logger.error("Error during initial summary generation attempt #{}: {}", attempts, e.getMessage(), e);
                try {
                    Thread.sleep(2000); // Wait 2 seconds after an exception
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.warn("Retry sleep interrupted", ie);
                }
            }
        }

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

    public PageSummaryModel generatePageSummary(PageModel page) {
        logger.info("Generating summary for page: {}", page.getId());
        String text = (page.getContent().trim().isEmpty()) ? "No content for this page" : page.getContent();
        String summaryText = (text.equals("No content for this page")) ? text : summarize(text, SummaryType.PAGE);

        PageSummaryModel summaryModel = new PageSummaryModel(summaryText);
        summaryModel.addPage(page);

        return summaryModel;
    }

    @Override
    public List<PageSummaryModel> generatePageSummaries(List<PageModel> pages) {
        logger.info("Generating page summaries sequentially for {} pages", pages.size());
        List<PageSummaryModel> summaries = new ArrayList<>();

        for (int j = 0; j < pages.size(); j++) {
            PageModel page = pages.get(j);
            String text = (page.getContent().trim().isEmpty()) ? "No content for this page" : page.getContent();
            String summaryText = (text.equals("No content for this page")) ? text : summarize(text, SummaryType.PAGE);

            PageSummaryModel summaryModel = new PageSummaryModel(summaryText);
            summaryModel.addPage(page);

            summaries.add(summaryModel);
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

        // Calculate total operations for more accurate progress tracking
        int totalOperations = calculateTotalChapterOperations(chapters);
        AtomicInteger completedOperations = new AtomicInteger(0);

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
                // Create a chapter-specific progress callback that maps to overall progress
                ProgressCallback chapterCallback = progress -> {
                    double overallProgress = (double) (completedOperations.get()
                            + (progress * estimateChapterOperations(text))) / totalOperations;
                    progressCallback.onProgress(overallProgress);
                };

                String summary = generateProgressiveSummary(text, SummaryType.CHAPTER, chapterCallback);
                ChapterSummaryModel summaryModel = new ChapterSummaryModel(summary);
                summaryModel.addChapter(chapter);
                summaries.add(summaryModel);

                // Update completed operations
                completedOperations.addAndGet(estimateChapterOperations(text));
            } catch (Exception e) {
                logger.error("Error processing chapter summary: {}", e.getMessage(), e);
                summaries.add(createEmptySummary(chapter, "Error processing chapter: " + e.getMessage(),
                        ChapterSummaryModel::new, ChapterSummaryModel::addChapter));
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
            return createEmptySummary(model, "Error processing document: " + e.getMessage(),
                    DocumentSummaryModel::new, DocumentSummaryModel::addDocument);
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
     * Generate progressive summary with progress tracking
     */
    private String generateProgressiveSummary(String text, SummaryType type, ProgressCallback progressCallback) {
        logger.debug("Starting progressive {} summary generation", type);
        List<String> parts = splitTextIntoParts(text, determineGroupSize(type));
        int totalParts = parts.size();
        AtomicInteger processedParts = new AtomicInteger(0);

        if (parts.isEmpty()) {
            return "No content available for summarization";
        }

        // Process first part
        String initialSummary = getInitialSummary(parts.get(0), type);
        updateProgress(processedParts.incrementAndGet(), totalParts, progressCallback);
        parts.remove(0);

        if (parts.isEmpty()) {
            return initialSummary;
        }

        // Process remaining parts
        StringBuilder currentSummary = new StringBuilder(initialSummary);
        for (String part : parts) {
            String instruction = switch (type) {
                case CHAPTER -> String.format(promptConfig.getChapterExpand(), currentSummary);
                case BOOK -> String.format(promptConfig.getBookExpand(), currentSummary);
                default -> String.format(promptConfig.getDefaultExpand(), currentSummary);
            };

            String newSummary = summarize(part, instruction);
            currentSummary.append("\n\n").append(newSummary);
            updateProgress(processedParts.incrementAndGet(), totalParts, progressCallback);
        }

        return currentSummary.toString();
    }

    private void updateProgress(int current, int total, ProgressCallback progressCallback) {
        if (progressCallback != null) {
            double progress = (double) current / total;
            progressCallback.onProgress(progress);
            logger.debug("Updated progress: {}/{} ({}%)", current, total, Math.round(progress * 100));
        }
    }

    // New helper methods for operation calculation

    /**
     * Calculates the total number of operations for all chapters
     */
    private int calculateTotalChapterOperations(List<ChapterModel> chapters) {
        return chapters.stream()
                .mapToInt(chapter -> {
                    String text = collectSummaries(chapter.getPages(), PageModel::getSummary,
                            summary -> summary.getContent());
                    return estimateChapterOperations(text);
                })
                .sum();
    }

    /**
     * Estimates the number of operations needed for a chapter based on text length
     */
    private int estimateChapterOperations(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        List<String> parts = splitTextIntoParts(text, determineGroupSize(SummaryType.CHAPTER));
        return parts.size();
    }
}
