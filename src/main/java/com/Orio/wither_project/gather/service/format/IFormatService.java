package com.Orio.wither_project.gather.service.format;

import java.util.List;

import com.Orio.wither_project.gather.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.ai.chat.model.ChatResponse;

public interface IFormatService {
    DataSource format(SearchResult searchResult);

    DataModel format(ScrapeResult scrapeResult);

    InformationPiece format(ProcessResult processResult);

    List<ScrapedTextBatch> formatPartsToProcess(List<ScrapeResult.ScrapeItem> items);

    List<QAModel> formatQAModels(ChatResponse response, String text) throws JsonProcessingException;
}
