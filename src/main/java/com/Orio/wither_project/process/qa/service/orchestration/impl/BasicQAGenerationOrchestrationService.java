package com.Orio.wither_project.process.qa.service.orchestration.impl;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.process.qa.model.QAModel;
import com.Orio.wither_project.process.qa.service.format.IAIResponseParser;
import com.Orio.wither_project.process.qa.service.generation.IQAGenerationService;
import com.Orio.wither_project.process.qa.service.generation.IQAResponseRefinementService;
import com.Orio.wither_project.process.qa.service.orchestration.IQAGenerationOrchestrationService;
import com.Orio.wither_project.process.qa.service.persistance.IQAPersistenceService;
import com.Orio.wither_project.socket.process.service.IQAProgressNotifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicQAGenerationOrchestrationService implements IQAGenerationOrchestrationService {

    private final IQAGenerationService qaGenerationService;
    private final IAIResponseParser aiResponseParser;
    private final IQAProgressNotifier qaProgressNotifier;
    private final IQAPersistenceService qaPersistenceService;
    private final IQAResponseRefinementService qaResponseRefinementService;

    @Override
    public ChatResponse processForQuestions(String text) {
        return qaGenerationService.generateQuestions(text);
    }

    @Override
    public ChatResponse processForAnswer(String text, String question) {
        return qaGenerationService.generateAnswer(text, question);
    }

    @Override
    public List<String> parseQuestions(ChatResponse response) {
        return aiResponseParser.parseQuestions(response);
    }

    @Override
    public QAModel parseQAModel(ChatResponse response) {
        return aiResponseParser.parseQAModel(response);
    }

    @Override
    public QAModel refine(QAModel qaModel, String content) {
        return qaResponseRefinementService.refine(qaModel, content);
    }

    @Override
    public void notifyWS(QAModel qaModel) {
        qaProgressNotifier.notifyQAResult(qaModel);
    }

    @Override
    public void save(QAModel qaModel) {
        qaPersistenceService.save(qaModel);
    }

}
