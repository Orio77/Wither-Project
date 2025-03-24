package com.Orio.wither_project.gather.service.orchestration.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.exception.InvalidQueryException;
import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.DataSource;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.ScrapeResult;
import com.Orio.wither_project.gather.model.SearchResult;
import com.Orio.wither_project.gather.service.format.IFormatService;
import com.Orio.wither_project.gather.service.orchestration.INewWitherOrchestrationService;
import com.Orio.wither_project.gather.service.scrape.IScrapeService;
import com.Orio.wither_project.gather.service.search.ISearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewWitherOrchestrationService implements INewWitherOrchestrationService {

    private final ISearchService searchService;
    private final IFormatService formatService;
    private final IScrapeService scrapeService;

    @Override
    public DataModel gatherData(String query) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("Query parameter is empty or missing");
            throw new InvalidQueryException("Query parameter is required and cannot be empty");
        }

        SearchResult searchResult = searchService.search(query);
        DataSource dataSource = formatService.format(searchResult);
        ScrapeResult scrapeResult = scrapeService.scrape(dataSource);
        DataModel dataModel = formatService.format(scrapeResult);
        return dataModel;
    }

    @Override
    public DataModel sendAndWait(DataModel dataModels) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendAndWait'");
    }

    @Override
    public List<InformationPiece> format(DataModel dataModels) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'format'");
    }

    @Override
    public void save(List<DataModel> dataModels) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public DataModel getDataModel(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDataModel'");
    }

    @Override
    public void processAndSave(DataModel dataModel) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processAndSave'");
    }

    @Override
    public ScrapeResult scrape(String url) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'scrape'");
    }

    @Override
    public DataModel format(ScrapeResult scrapeResult) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'format'");
    }

    @Override
    public void save(DataModel dataModel) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

}
