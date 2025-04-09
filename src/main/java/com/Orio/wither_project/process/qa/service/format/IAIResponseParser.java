package com.Orio.wither_project.process.qa.service.format;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;

import com.Orio.wither_project.process.qa.model.QAModel;

public interface IAIResponseParser {

    List<String> parseQuestions(ChatResponse response);

    QAModel parseQAModel(ChatResponse response);
}
