package com.Orio.wither_project.gather.service.process.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.gather.config.TestTextConfig;
import com.Orio.wither_project.gather.model.QAModel;
import com.fasterxml.jackson.core.JsonProcessingException;

@SpringBootTest
@ActiveProfiles("test")
public class OllamaQAServiceTest {

    @Autowired
    private OllamaQAService ollamaQAService;

    @Autowired
    private TestTextConfig testContentConfig;

    @Test
    public void testExtractShortClean() throws JsonProcessingException {
        // Arrange
        String content = testContentConfig.getShortGeneratedText();

        // Act
        List<QAModel> result = ollamaQAService.extract(content);

        // Assert
        assertNotNull(result, "The extraction result should not be null");
        assertFalse(result.isEmpty(), "The extraction should produce at least one QA model");

        // Check properties of the first QA model
        QAModel firstModel = result.get(0);
        assertNotNull(firstModel.getQuestion(), "Question should not be null");
        assertNotNull(firstModel.getAnswer(), "Answer should not be null");
        assertFalse(firstModel.getQuestion().isEmpty(), "Question should not be empty");
        assertFalse(firstModel.getAnswer().isEmpty(), "Answer should not be empty");
    }

    @Test
    public void testExtractScrapedMessy() throws JsonProcessingException {
        // Arrange
        String content = testContentConfig.getLongScrapedText();

        // Act
        List<QAModel> result = ollamaQAService.extract(content);

        // Assert
        assertNotNull(result, "The extraction result should not be null");
        assertFalse(result.isEmpty(), "The extraction should produce at least one QA model");

        // Check properties of the first QA model
        QAModel firstModel = result.get(0);
        assertNotNull(firstModel.getQuestion(), "Question should not be null");
        assertNotNull(firstModel.getAnswer(), "Answer should not be null");
        assertFalse(firstModel.getQuestion().isEmpty(), "Question should not be empty");
        assertFalse(firstModel.getAnswer().isEmpty(), "Answer should not be empty");
    }
}
