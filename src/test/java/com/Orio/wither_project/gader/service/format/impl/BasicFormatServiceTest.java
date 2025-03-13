package com.Orio.wither_project.gader.service.format.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.gader.model.QAModel;
import com.Orio.wither_project.gader.service.format.IFormatService;

@SpringBootTest
@ActiveProfiles("test")
public class BasicFormatServiceTest {

    @Autowired
    private IFormatService formatService;

    @Test
    public void testFormatQAModels() throws Exception {
        // Setup test data - JSON response with QA pairs
        String jsonContent = "{"
                + "\"qaPairs\": ["
                + "  {"
                + "    \"question\": \"What is Spring Boot?\","
                + "    \"first_three_words_of_an_answer\": \"Spring Boot is\","
                + "    \"last_three_words_of_an_answer\": \"development of applications\""
                + "  }"
                + "]}";

        // Text content that contains the answer
        String textContent = "Spring Boot is a framework that makes it easier for developers to create standalone, "
                + "production-grade Spring-based Applications with minimal effort. It provides defaults for code "
                + "and annotation configuration to simplify the development of applications.";

        // Create a real ChatResponse with our test content
        AssistantMessage assistantMessage = new AssistantMessage(jsonContent);
        Generation generation = new Generation(assistantMessage);
        ChatResponse chatResponse = new ChatResponse(Collections.singletonList(generation));

        // Execute the method with the real ChatResponse and text content
        List<QAModel> result = formatService.formatQAModels(chatResponse, textContent);

        // Verify results
        assertEquals(1, result.size());
        assertEquals("What is Spring Boot?", result.get(0).getQuestion());

        // The answer should be extracted from textContent between "Spring Boot is" and
        // "development of applications."
        String expectedAnswer = "Spring Boot is a framework that makes it easier for developers to create standalone, "
                + "production-grade Spring-based Applications with minimal effort. It provides defaults for code "
                + "and annotation configuration to simplify the development of applications";
        assertEquals(expectedAnswer, result.get(0).getAnswer());
    }
}
