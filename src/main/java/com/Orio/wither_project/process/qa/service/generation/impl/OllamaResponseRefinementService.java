package com.Orio.wither_project.process.qa.service.generation.impl;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.process.qa.config.AIQAPromptConfig;
import com.Orio.wither_project.process.qa.model.QAModel;
import com.Orio.wither_project.process.qa.service.generation.IQAResponseRefinementService;
import com.Orio.wither_project.util.AIResponseParser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaResponseRefinementService implements IQAResponseRefinementService {

    private final OllamaChatModel ollamaChatModel;
    private final AIQAPromptConfig promptConfig;

    @Override
    public QAModel refine(QAModel qaModel, String content) {
        log.info("Starting refinement for question: '{}'", qaModel.getQuestion());
        log.debug("Original answer: '{}'", qaModel.getAnswer());
        log.debug("Content length: {}", content.length());

        Prompt prompt = promptConfig.getRefinementPrompt(
                content,
                qaModel.getQuestion(),
                qaModel.getAnswer());

        ChatResponse response = ollamaChatModel.call(prompt);
        log.debug("Received refinement response from Ollama model: {}", response.getResult().getOutput().getContent());

        QAModel refinedModel = AIResponseParser.parseResponseToObject(response, QAModel.class);

        if (refinedModel.getAnswer() != null && !refinedModel.getAnswer().isBlank()) {
            log.info("Refined answer obtained.");
            qaModel.setAnswer(refinedModel.getAnswer().trim()); // Update the model
        } else {
            log.warn("Could not extract refined answer from response: {}", refinedModel);
        }

        return qaModel;
    }
}
