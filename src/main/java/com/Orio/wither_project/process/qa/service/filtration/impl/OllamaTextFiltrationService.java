package com.Orio.wither_project.process.qa.service.filtration.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.core.config.OllamaConfig;
import com.Orio.wither_project.gather.model.TextBatch;
import com.Orio.wither_project.gather.service.format.IProcessingFormatService;
import com.Orio.wither_project.process.qa.config.AITextFiltrationPromptConfig;
import com.Orio.wither_project.process.qa.service.filtration.ITextFiltrationService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class OllamaTextFiltrationService implements ITextFiltrationService {

    private OllamaChatModel ollamaChatModel;
    private final OllamaConfig ollamaConfig;
    private final AITextFiltrationPromptConfig promptConfig;
    private final IProcessingFormatService formatService;
    private final String customModel = "qwen2:latest";

    @Override
    public List<TextBatch> filter(List<TextBatch> textBatches) {
        log.info("Filtering text batches: {}", textBatches.size());
        ollamaChatModel = ollamaConfig.getCustomModelChatModel(customModel);
        log.info("Filtering with model: {}", ollamaChatModel.getDefaultOptions().getModel());

        // Process batches in parallel
        List<CompletableFuture<TextBatch>> batchFutures = textBatches.stream()
                .map(textBatch -> CompletableFuture.supplyAsync(() -> processTextBatch(textBatch)))
                .collect(Collectors.toList());

        // Collect only non-null results (batches with valuable content)
        List<TextBatch> filtered = batchFutures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        log.error("Thread was interrupted while waiting for batch processing", e);
                        Thread.currentThread().interrupt(); // Restore interrupted status
                        return null;
                    } catch (ExecutionException e) {
                        log.error("Execution exception occurred while processing batch", e);
                        return null;
                    }
                })
                .filter(batch -> batch != null)
                .collect(Collectors.toList());

        log.info("Filtered text batches: {}", filtered.size());
        return filtered;
    }

    private TextBatch processTextBatch(TextBatch textBatch) {
        List<String> contents = textBatch.getContent();

        // Process content items in parallel using CompletableFuture
        List<CompletableFuture<String>> futures = contents.stream()
                .map(content -> CompletableFuture.supplyAsync(() -> {
                    if (isValuable(content)) {
                        return content;
                    }
                    return null;
                }))
                .collect(Collectors.toList());

        // Collect only non-null results (valuable content)
        List<String> valuableContent = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException e) {
                        log.error("Thread was interrupted while waiting for content evaluation", e);
                        Thread.currentThread().interrupt(); // Restore interrupted status
                        return null;
                    } catch (ExecutionException e) {
                        log.error("Execution exception occurred while evaluating content", e);
                        return null;
                    }
                })
                .filter(content -> content != null)
                .collect(Collectors.toList());

        if (valuableContent.isEmpty()) {
            return null;
        }

        return TextBatch.builder()
                .source(textBatch.getSource())
                .content(valuableContent)
                .build();
    }

    private boolean isValuable(String text) {
        ChatResponse response = ollamaChatModel.call(promptConfig.getTextFiltrationPrompt(text));
        try {
            return parseResponse(response);
        } catch (JsonProcessingException e) {
            // TODO: add fallback method
            return false;
        }
    }

    private boolean parseResponse(ChatResponse response) throws JsonProcessingException {
        return formatService.parseValuableVerdict(response);
    }

}
