package com.Orio.wither_project.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class ResoningModelTest {

    @Autowired
    private OllamaChatModel chatModel;

    @Autowired
    private OllamaOptions options;

    private final String prompt = "Calculate Earth's distance from the sun in 2300";
    private final String reasoningModel = "deepseek-r1:7b-qwen-distill-q4_K_M";

    @BeforeEach
    public void setUp() {
        options.setModel(reasoningModel);
    }

    @Test
    public void getOutput() {

        log.info("Chat model: {}", chatModel.getDefaultOptions().getModel());

        ChatResponse response = chatModel.call(new Prompt(prompt, chatModel.getDefaultOptions()));

        String content = response.getResult().getOutput().getContent();

        log.info("Chat response: {}", content);

        assertNotNull(response);
        assertTrue(content != null && !content.isEmpty());

        String response2 = parseResponse(content);
        log.info("Parsed response: {}", response2);

        assertTrue(response2 != null && !response2.isEmpty());
    }

    private String parseResponse(String response) {
        final String endPoint = "</think>";
        int indexOf = response.indexOf(endPoint);
        String subs = response.substring(indexOf + endPoint.length());
        return indexOf != -1 ? subs.trim() : response;
    }
}
