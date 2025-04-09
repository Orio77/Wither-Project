package com.Orio.wither_project.gather.service.format;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;

import com.Orio.wither_project.gather.model.ContentWithSource;
import com.Orio.wither_project.gather.model.TextBatch;
import com.Orio.wither_project.process.qa.model.QAModel;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IProcessingFormatService {

    List<TextBatch> formatPartsToProcess(List<ContentWithSource> items);

    List<TextBatch> formatPartsToProcess(ContentWithSource item);

    List<QAModel> formatQAModels(ChatResponse response, String text) throws JsonProcessingException;

    boolean parseValuableVerdict(ChatResponse response) throws JsonProcessingException;

}
