package com.Orio.wither_project.gather.service.format.impl;

import java.util.ArrayList;
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
import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.model.ScrapedTextBatch;
import com.Orio.wither_project.gather.model.SearchResult;
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
        return InformationPiece.builder().build();
    }

    @Override
    public List<InformationPiece> format(DataModel dataModel) {
        log.info("Formatting data model with query: {}", dataModel.getQuery());

        List<ScrapeItem> items = dataModel.getItems();
        List<InformationPiece> res = new ArrayList<>();

        if (items == null || items.isEmpty()) {
            log.warn("No items found in data model for query: {}", dataModel.getQuery());
            return res;
        }

        log.debug("Processing {} items from data model", items.size());
        items.forEach(item -> {
            log.trace("Creating information piece for item with title: {}", item.getTitle());
            InformationPiece piece = InformationPiece.builder()
                    .query(dataModel.getQuery())
                    .error(dataModel.getErrors())
                    .author(item.getAuthor())
                    .content(item.getContent())
                    .description(item.getDescription())
                    .link(item.getLink())
                    .publishDate(item.getPublishDate())
                    .title(item.getTitle())
                    .build();
            res.add(piece);
        });

        log.info("Formatted {} information pieces from data model", res.size());
        return res;
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
