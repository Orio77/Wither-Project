package com.Orio.wither_project.gather.service.format.impl;

import com.Orio.wither_project.gather.exception.AnswerNotFoundException;
import com.Orio.wither_project.gather.model.QAModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for extracting QA models from AI chat responses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QAModelExtractionService {

    private static final int LOG_CONTENT_PREVIEW_LENGTH = 100;
    private static final String QA_PAIRS_PATH = "qaPairs";
    private static final String FIRST_WORDS_PATH = "first_three_words_of_an_answer";
    private static final String LAST_WORDS_PATH = "last_three_words_of_an_answer";
    private static final String QUESTION_PATH = "question";

    private final ObjectMapper objectMapper;
    private final TextSearchService textSearchService;

    /**
     * Extracts question-answer models from a chat response using the provided text.
     *
     * @param response The chat response containing QA pairs
     * @param text     The source text to extract answers from
     * @return List of extracted QAModels
     * @throws JsonProcessingException If the response cannot be parsed as JSON
     */
    public List<QAModel> extractQAModels(ChatResponse response, String text) throws JsonProcessingException {
        Objects.requireNonNull(response, "Response cannot be null");
        Objects.requireNonNull(text, "Text cannot be null");

        log.info("Starting to extract QA models from response using text: {}",
                truncateForLogging(text));

        List<QAModel> qaModels = new ArrayList<>();
        String responseContent = extractResponseContent(response);

        try {
            JsonNode rootNode = objectMapper.readTree(responseContent);
            JsonNode qaPairsNode = rootNode.path(QA_PAIRS_PATH);

            if (qaPairsNode.isArray()) {
                processQAPairs(qaPairsNode, text, qaModels);
            } else {
                log.warn("No QA pairs array found in response");
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response: {}", e.getMessage());
            throw e;
        }

        return qaModels;
    }

    /**
     * Extracts and logs the content from a chat response.
     */
    private String extractResponseContent(ChatResponse response) {
        String content = response.getResult().getOutput().getContent();
        log.debug("Parsing content: {}", truncateForLogging(content));
        return content;
    }

    /**
     * Processes the QA pairs node and builds QA models.
     */
    private void processQAPairs(JsonNode qaPairsNode, String text, List<QAModel> qaModels) {
        log.info("Found {} QA pairs in response", qaPairsNode.size());
        int processedCount = 0;
        int validCount = 0;

        for (JsonNode pairNode : qaPairsNode) {
            processedCount++;
            QAPairData pairData = extractQAPairData(pairNode);

            if (!isValidQAPair(pairData)) {
                log.warn("Skipping incomplete QA pair at position {}", processedCount);
                continue;
            }

            validCount++;
            log.debug("Processing QA pair {}: Q: '{}', First words: '{}', Last words: '{}'",
                    processedCount, pairData.question, pairData.firstThreeWords, pairData.lastThreeWords);

            tryAddQAModel(pairData, text, qaModels);
        }

        log.info("Successfully processed {}/{} valid QA pairs, created {} QA models",
                validCount, processedCount, qaModels.size());
    }

    /**
     * Extracts question and answer data from a JSON node.
     */
    private QAPairData extractQAPairData(JsonNode pairNode) {
        String firstThreeWords = pairNode.path(FIRST_WORDS_PATH).asText("");
        String lastThreeWords = pairNode.path(LAST_WORDS_PATH).asText("");
        String question = pairNode.path(QUESTION_PATH).asText("");

        return new QAPairData(firstThreeWords, lastThreeWords, question);
    }

    /**
     * Validates if a QA pair has all required fields.
     */
    private boolean isValidQAPair(QAPairData pairData) {
        return !pairData.firstThreeWords.isEmpty() &&
                !pairData.lastThreeWords.isEmpty() &&
                !pairData.question.isEmpty();
    }

    /**
     * Attempts to create and add a QA model from the pair data.
     */
    private void tryAddQAModel(QAPairData pairData, String text, List<QAModel> qaModels) {
        try {
            String answer = textSearchService.findAnswer(
                    pairData.firstThreeWords, pairData.lastThreeWords, text);

            QAModel qaModel = QAModel.builder()
                    .question(pairData.question)
                    .answer(answer)
                    .build();

            qaModels.add(qaModel);
            log.debug("Added QA model with answer length: {}", answer.length());
        } catch (AnswerNotFoundException e) {
            log.warn("Could not find answer for question: '{}'. {}",
                    pairData.question, e.getMessage());
        }
    }

    /**
     * Truncates text for logging purposes.
     */
    private String truncateForLogging(String text) {
        if (text == null)
            return "null";
        return text.length() > LOG_CONTENT_PREVIEW_LENGTH ? text.substring(0, LOG_CONTENT_PREVIEW_LENGTH) + "..."
                : text;
    }

    /**
     * Data class to hold QA pair information.
     */
    private static class QAPairData {
        final String firstThreeWords;
        final String lastThreeWords;
        final String question;

        QAPairData(String firstThreeWords, String lastThreeWords, String question) {
            this.firstThreeWords = firstThreeWords;
            this.lastThreeWords = lastThreeWords;
            this.question = question;
        }
    }
}
