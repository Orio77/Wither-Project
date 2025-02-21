package com.Orio.wither_project.summary.service.generation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.socket.summary.model.SummaryProgressDTO;
import com.Orio.wither_project.summary.config.SummaryPromptConfig;
import com.Orio.wither_project.summary.model.PageModel;
import com.Orio.wither_project.summary.model.PageSummaryModel;
import com.Orio.wither_project.summary.model.SummaryResponse;
import com.Orio.wither_project.summary.model.SummaryType;
import com.Orio.wither_project.summary.service.generation.IPDFSummaryGenerationService;
import com.Orio.wither_project.summary.service.storage.ISQLDocumentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OllamaSummaryGenerationService implements IPDFSummaryGenerationService { // TODO make multithreaded in
                                                                                      // another class
    private static final int MAX_RETRIES = 3;
    private static final long WAIT_TIME_MS = 20000; // 20 seconds
    private static final String SUMMARIZATION_FAILED_MESSAGE = "Failed to generate a summary. See the log for details";

    private static final Logger logger = LoggerFactory.getLogger(OllamaSummaryGenerationService.class);

    private final OllamaChatModel ollamaChatModel;

    private final SummaryPromptConfig promptConfig;

    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper;

    private final ISQLDocumentService sqlDocumentService;

    @Override
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

    @Override
    public String summarize(String text, String instruction) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                logger.info("Attempt {} of {} to generate summary", attempts + 1, MAX_RETRIES);
                return summarize(text, instruction, "this argument is to be removed"); // TODO
            } catch (Exception e) {
                attempts++;
                logger.warn("Attempt {} failed, reason: {}", attempts, e.getMessage());

                if (attempts < MAX_RETRIES) {
                    try {
                        logger.info("Waiting {} seconds before next retry...", WAIT_TIME_MS / 1000);
                        Thread.sleep(WAIT_TIME_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }

        logger.error("All {} retry attempts failed", MAX_RETRIES);
        return SUMMARIZATION_FAILED_MESSAGE;
    }

    // !RETRY
    // ! Delete the method above and uncomment this one
    // @Override
    // public String summarize(String text, String instruction) {
    // try {
    // logger.info("Attempting to generate summary");
    // return summarize(text, instruction, promptConfig.getSummaryJsonSchema());
    // } catch (Exception e) {
    // logger.error("Failed to generate summary after multiple retries: {}",
    // e.getMessage(), e);
    // return SUMMARIZATION_FAILED_MESSAGE;
    // }
    // }

    @Override
    public String summarize(String text, String instruction, String responseFormat) {
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
                    return "Unable to parse summary from model response.";
                }
            } else {
                logger.warn("Received null response or result from Ollama model");
                return "Unable to generate summary: no response from model.";
            }

        } catch (Exception e) {
            logger.error("Failed to generate summary: {}", e.getMessage(), e);
            return String.format("Error during summary generation: %s", e.getMessage());
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
        String[] parts = text.split("\n\n\n\n"); // TODO Extract to constant
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
        AtomicInteger i = new AtomicInteger(0);
        List<PageSummaryModel> summaries = new ArrayList<>();
        List<PageModel> pagesToSave = new ArrayList<>();
        int totalPages = pages.size();

        for (int j = 0; j < pages.size(); j++) {
            PageModel page = pages.get(j);
            final int curPage = i.incrementAndGet();
            String text = (page.getContent().trim().isEmpty()) ? "No content for this page" : page.getContent();
            String summaryText = summarize(text, SummaryType.PAGE);

            PageSummaryModel summaryModel = new PageSummaryModel(summaryText);
            summaryModel.addPage(page);
            pagesToSave.add(page);

            if ((j + 1) % 5 == 0 || j == pages.size() - 1) {
                sqlDocumentService.savePages(pagesToSave);
                pagesToSave.clear();
            }

            SummaryProgressDTO progressUpdate = new SummaryProgressDTO(curPage, totalPages);
            logger.info("Messaging template: " + messagingTemplate);
            logger.info("Sending progress update: {}", progressUpdate);
            messagingTemplate.convertAndSend("/topic/progress", progressUpdate);

            summaries.add(summaryModel);
        }
        return summaries;
    }
}
