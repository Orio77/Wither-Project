package com.Orio.wither_project.gather.service.orchestration.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.exception.InvalidQueryException;
import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.DataSource;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.ScrapeResult;
import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.model.SearchResult;
import com.Orio.wither_project.gather.repository.InformationPieceRepo;
import com.Orio.wither_project.gather.service.format.IModelFormatService;
import com.Orio.wither_project.gather.service.orchestration.IGatherOrchestrationService;
import com.Orio.wither_project.gather.service.scrape.IScrapeService;
import com.Orio.wither_project.gather.service.search.ISearchService;
import com.Orio.wither_project.socket.gather.service.impl.ScrapeItemReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GatherOrchestrationService implements IGatherOrchestrationService {

    private final ISearchService searchService;
    private final IModelFormatService modelFormatService;
    private final IScrapeService scrapeService;
    private final ScrapeItemReviewService dataModelReviewService;
    private final InformationPieceRepo infoRepo;

    @Override
    public DataModel gatherData(String query) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("Query parameter is empty or missing");
            throw new InvalidQueryException("Query parameter is required and cannot be empty");
        }

        log.info("Starting data gathering process for query: {}", query);
        SearchResult searchResult = searchService.search(query);
        log.debug("Search completed, found {} results", searchResult.getItems().size());

        DataSource dataSource = modelFormatService.format(searchResult);
        log.debug("Search results formatted into data source");

        ScrapeResult scrapeResult = scrapeService.scrape(dataSource);
        log.debug("Scraping completed, processed {} items", scrapeResult.getItems().size());

        DataModel dataModel = modelFormatService.format(scrapeResult);
        log.info("Data gathering completed successfully for query: {}", query);
        return dataModel;
    }

    @Override
    public DataModel sendAndWait(DataModel dataModel) {
        log.info("Sending {} items for review", dataModel.getItems().size());
        List<ScrapeItem> res = dataModelReviewService.sendForReviewAndWait(dataModel.getItems());
        log.debug("Received {} reviewed items", res.size());
        dataModel.setItems(res);
        return dataModel;
    }

    @Override
    public List<InformationPiece> format(DataModel dataModel) {
        return modelFormatService.format(dataModel);
    }

    @Override
    public void save(List<InformationPiece> dataModels) {
        infoRepo.saveAllAndFlush(dataModels);
    }

    @Override
    public ScrapeResult scrape(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'scrape'");
    }

    @Override
    public DataModel format(ScrapeResult scrapeResult) {
        return modelFormatService.format(scrapeResult);
    }

}
