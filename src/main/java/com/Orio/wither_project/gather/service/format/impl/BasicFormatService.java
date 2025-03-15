package com.Orio.wither_project.gather.service.format.impl;

import java.util.List;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.model.ContentWithSource;
import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.DataSource;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.ProcessResult;
import com.Orio.wither_project.gather.model.QAModel;
import com.Orio.wither_project.gather.model.ScrapeResult;
import com.Orio.wither_project.gather.model.ScrapedTextBatch;
import com.Orio.wither_project.gather.model.SearchResult;
import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.service.format.IFormatService;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicFormatService implements IFormatService {

    private final TextSplitService textSplitterService;
    private final QAModelExtractionService qaModelExtractorService;

    @Override
    public DataSource format(SearchResult searchResult) {
        log.info("Formatting search result " + searchResult);
        return DataSource.builder()
                .query(searchResult.getQuery())
                .items(searchResult.getItems())
                .errors(searchResult.getErrors())
                .build();
    }

    @Override
    public DataModel format(ScrapeResult scrapeResult) {
        return DataModel.builder()
                .query(scrapeResult.getQuery())
                .items(scrapeResult.getItems())
                .errors(scrapeResult.getErrors())
                .build();
    }

    @Override
    public InformationPiece format(ProcessResult processResult) {
        log.info("Formatting process result " + processResult);
        // Add logic for formatting processResult
        return new InformationPiece();
    }

    @Override
    public List<ScrapedTextBatch> formatPartsToProcess(List<ScrapeItem> items) {
        log.info("Formatting {} items into processing parts", items.size());
        List<ContentWithSource> contentWithSources = textSplitterService.getContentWithSources(items);
        return textSplitterService.splitContent(contentWithSources);
    }

    @Override
    public List<QAModel> formatQAModels(ChatResponse response, String text) throws JsonProcessingException {
        return qaModelExtractorService.extractQAModels(response, text);
    }
}
