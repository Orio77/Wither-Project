package com.Orio.wither_project.process.qa.service.generation.impl;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.prompt.Prompt;

import com.Orio.wither_project.process.qa.config.AIQAPromptConfig;
import com.Orio.wither_project.process.qa.service.generation.IQAGenerationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaQAGenerationService implements IQAGenerationService {

    private final OllamaChatModel ollamaChatModel;
    private final AIQAPromptConfig promptConfig;

    @Override
    public ChatResponse generateQuestions(String text) {
        logOperationStart("question generation", text);
        Prompt prompt = promptConfig.getQuestionGenerationPrompt(text);
        ChatResponse response = ollamaChatModel.call(prompt);
        logResponseReceived(response);
        return response;
    }

    @Override
    public ChatResponse generateAnswer(String text, String question) {
        logOperationStart("answer generation", text);
        log.debug("Question: {}", question);
        Prompt prompt = promptConfig.getAnswerGenerationPrompt(text, question);
        ChatResponse response = ollamaChatModel.call(prompt);
        logResponseReceived(response);
        return response;
    }

    private void logOperationStart(String operation, String text) {
        log.debug("Starting {} with model: {}", operation, ollamaChatModel.getDefaultOptions().getModel());
        log.info("Starting {} from content of length: {}", operation, text.length());
        // Use formatted string for cleaner log message construction
        log.debug("Content excerpt: {}", text.length() > 100 ? text.substring(0, 100) + "..." : text);
    }

    private void logResponseReceived(ChatResponse response) {
        // Use helper method to safely get content, handling potential nulls
        String content = response != null && response.getResult() != null && response.getResult().getOutput() != null
                ? response.getResult().getOutput().getContent()
                : "[No content in response]";
        log.debug("Received response from Ollama model: {}", content);
    }
}