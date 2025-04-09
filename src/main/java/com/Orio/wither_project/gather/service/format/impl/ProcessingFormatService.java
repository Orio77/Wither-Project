package com.Orio.wither_project.gather.service.format.impl;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.model.ContentWithSource;
import com.Orio.wither_project.gather.model.TextBatch;
import com.Orio.wither_project.gather.service.format.IProcessingFormatService;
import com.Orio.wither_project.process.qa.model.QAModel;
import com.Orio.wither_project.process.qa.service.format.impl.QAModelExtractionService;
import com.Orio.wither_project.process.qa.service.format.impl.TextSplitService;
import com.Orio.wither_project.util.AIResponseParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingFormatService implements IProcessingFormatService {

    private final TextSplitService textSplitterService;
    private final QAModelExtractionService qaModelExtractorService;
    private final ObjectMapper objectMapper;

    @Override
    public List<TextBatch> formatPartsToProcess(List<ContentWithSource> items) {
        log.info("Formatting {} items into processing parts", items.size());
        return textSplitterService.splitContent(items);
    }

    @Override
    public List<TextBatch> formatPartsToProcess(ContentWithSource item) {
        log.info("Formatting single item into processing parts");
        return textSplitterService.splitContent(List.of(item));
    }

    @Override
    public List<QAModel> formatQAModels(ChatResponse response, String text) throws JsonProcessingException {
        log.info("Formatting QA models from chat response");
        return qaModelExtractorService.extractQAModels(response, text);
    }

    @Override
    public boolean parseValuableVerdict(ChatResponse response) throws JsonProcessingException {
        String content = AIResponseParser.parseResponse(response);

        JsonNode rootNode = objectMapper.readTree(content);

        if (rootNode.has("is_valuable")) {
            String isValuableValue = rootNode.get("is_valuable").asText();
            return "1".equals(isValuableValue);
        }

        log.warn("Unexpected response format in parseValuableVerdict: {}", content);
        return false;
    }

}
