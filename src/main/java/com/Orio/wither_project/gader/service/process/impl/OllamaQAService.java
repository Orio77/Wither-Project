package com.Orio.wither_project.gader.service.process.impl;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gader.config.AIPromptConfig;
import com.Orio.wither_project.gader.model.QAModel;
import com.Orio.wither_project.gader.service.format.IFormatService;
import com.Orio.wither_project.gader.service.process.IQAService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaQAService implements IQAService {

    private final OllamaChatModel ollamaChatModel;
    private final AIPromptConfig promptConfig;
    private final IFormatService formatService;

    public List<QAModel> extract(String text) throws JsonProcessingException {
        log.debug("Starting qa extraction with model: {}", ollamaChatModel.getDefaultOptions().getModel());
        log.info("Starting QA extraction from content of length: {}", text.length());
        log.debug("Content excerpt: {}", text.length() > 100 ? text.substring(0, 100) + "..." : text);

        ChatResponse response = ollamaChatModel.call(promptConfig.getQAExtractionPrompt(text));
        log.debug("Received response from Ollama model: {}", response.getResult().getOutput().getContent());

        List<QAModel> qaModels = formatService.formatQAModels(response, text);
        log.info("Successfully extracted {} QA models from content", qaModels.size());

        return qaModels;
    }

}
