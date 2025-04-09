package com.Orio.wither_project.process.qa.service.generation;

import org.springframework.ai.chat.model.ChatResponse;

public interface IQAGenerationService {

    ChatResponse generateQuestions(String text);

    ChatResponse generateAnswer(String text, String question);

}
