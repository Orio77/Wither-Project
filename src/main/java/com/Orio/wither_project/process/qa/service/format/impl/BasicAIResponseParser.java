package com.Orio.wither_project.process.qa.service.format.impl;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.process.qa.model.QAModel;
import com.Orio.wither_project.process.qa.service.format.IAIResponseParser;
import com.Orio.wither_project.util.AIResponseParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BasicAIResponseParser implements IAIResponseParser {

    @Override
    public List<String> parseQuestions(ChatResponse response) {
        log.debug("Starting to parse questions from AI response");
        @SuppressWarnings("unchecked")
        Map<String, List<String>> rawList = AIResponseParser.parseResponseToObject(response, Map.class);
        log.debug("Raw response parsed to list with {} items", rawList.size());

        var result = rawList.get("questions");

        log.debug("Successfully parsed {} questions from AI response", result.size());
        return result;
    }

    @Override
    public QAModel parseQAModel(ChatResponse response) {
        log.debug("Starting to parse QA model from AI response");
        @SuppressWarnings("unchecked")
        Map<String, String> qa = AIResponseParser.parseResponseToObject(response, Map.class);

        var result = QAModel.builder()
                .question(qa.get("question"))
                .answer(qa.get("answer"))
                .build();

        log.debug("Successfully parsed QA model from AI response");
        return result;
    }
}
