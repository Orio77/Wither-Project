package com.Orio.wither_project.process.qa.service.generation.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.process.qa.service.generation.IQAGenerationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class IQAGenerationServiceTest {

    @Autowired
    private IQAGenerationService qaGenerationService;

    private final String sampleText = """
            The Eiffel Tower is a wrought-iron lattice tower on the Champ de Mars in Paris, France.
            It is named after the engineer Gustave Eiffel, whose company designed and built the tower.
            Constructed from 1887 to 1889 as the entrance to the 1889 World's Fair,
            it was initially criticized by some of France's leading artists and intellectuals for its design,
            but it has become a global cultural icon of France and one of the most recognizable structures in the world.
            """;
    private final String emptyText = "";
    private final String irrelevantQuestion = "What is the color of the sky?";

    @Test
    void generateQuestions_withValidText_shouldReturnResponse() {
        ChatResponse response = qaGenerationService.generateQuestions(sampleText);
        assertValidResponse(response);
    }

    @Test
    void generateQuestions_withEmptyText_shouldReturnResponse() {
        ChatResponse response = qaGenerationService.generateQuestions(emptyText);
        assertNotNull(response, "Response should not be null even for empty text");
    }

    @Test
    void generateAnswer_withValidTextAndQuestion_shouldReturnResponse() {
        String question = "Who designed the Eiffel Tower?";
        ChatResponse response = qaGenerationService.generateAnswer(sampleText, question);
        assertValidResponse(response);
    }

    @Test
    void generateAnswer_withValidTextAndIrrelevantQuestion_shouldReturnResponse() {
        ChatResponse response = qaGenerationService.generateAnswer(sampleText, irrelevantQuestion);
        assertValidResponse(response);
    }

    @Test
    void generateAnswer_withEmptyText_shouldReturnResponse() {
        String question = "Who designed the Eiffel Tower?";
        ChatResponse response = qaGenerationService.generateAnswer(emptyText, question);
        assertNotNull(response, "Response should not be null even for empty text");
    }

    @Test
    void generateAnswer_withEmptyQuestion_shouldReturnResponse() {
        String emptyQuestion = "";
        ChatResponse response = qaGenerationService.generateAnswer(sampleText, emptyQuestion);
        assertNotNull(response, "Response should not be null even for empty question");
    }

    @Test
    void generateQuestionsAndThenAnswer_withValidText_shouldChainSuccessfully() {
        // 1. Generate Questions
        ChatResponse questionResponse = qaGenerationService.generateQuestions(sampleText);
        assertValidResponse(questionResponse);
        String generatedQuestionsContent = questionResponse.getResult().getOutput().getContent();

        // 2. Attempt to extract the first question (simple parsing, might need
        // adjustment)
        String firstQuestion = extractFirstQuestion(generatedQuestionsContent);
        assumeQuestionExtracted(firstQuestion); // Skip test if parsing fails

        // 3. Generate Answer for the extracted question
        log.info("Attempting to answer generated question: '{}'", firstQuestion);
        ChatResponse answerResponse = qaGenerationService.generateAnswer(sampleText, firstQuestion);
        assertValidResponse(answerResponse);
    }

    // --- Helper Methods ---

    private void assertValidResponse(ChatResponse response) {
        assertNotNull(response, "ChatResponse should not be null");
        assertNotNull(response.getResult(), "ChatResponse result should not be null");
        Generation generation = response.getResult();
        assertNotNull(generation.getOutput(), "Generation output should not be null");
        String content = generation.getOutput().getContent();
        assertNotNull(content, "Generation content should not be null");
        assertFalse(content.isBlank(), "Generation content should not be blank");
        log.info("Generated Content: {}",
                content.length() > 100 ? content.substring(0, 100) + "..." : content); // Using logger instead of
                                                                                       // System.out
    }

    private String extractFirstQuestion(String content) {
        if (content == null)
            return null;
        // Try matching patterns like "1. Question?" or "- Question?"
        Pattern pattern = Pattern.compile("^[\\s*\\-]*\\d*\\.*\\s*(.*?\\?)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null; // Or throw an exception if a question is strictly expected
    }

    private void assumeQuestionExtracted(String question) {
        org.junit.jupiter.api.Assumptions.assumeTrue(question != null && !question.isBlank(),
                "Could not extract a valid question from the generated content.");
    }
}
