package com.Orio.wither_project.gader.service.orchestration.impl;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gader.model.DataModel;
import com.Orio.wither_project.gader.model.DataSource;
import com.Orio.wither_project.gader.model.InformationPiece;
import com.Orio.wither_project.gader.model.ProcessResult;
import com.Orio.wither_project.gader.model.ScrapeResult;
import com.Orio.wither_project.gader.model.SearchResult;
import com.Orio.wither_project.gader.service.format.IFormatService;
import com.Orio.wither_project.gader.service.orchestration.IWitherOrchestrationService;
import com.Orio.wither_project.gader.service.persist.IPersistenceService;
import com.Orio.wither_project.gader.service.scrape.IScrapeService;
import com.Orio.wither_project.gader.service.search.ISearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WitherOrchestrationService implements IWitherOrchestrationService {

    private final ISearchService searchService;
    private final IScrapeService scrapeService;
    private final IFormatService formatService;
    private final IPersistenceService persistenceService;
    private final ProcessingOrchestrationService processingOrchestrationService;

    @Override
    public SearchResult search(String query) {
        return searchService.search(query);
    }

    @Override
    public DataSource format(SearchResult searchResult) {
        return formatService.format(searchResult);
    }

    @Override
    public ScrapeResult scrape(DataSource dataSource) {
        return scrapeService.scrape(dataSource);
    }

    @Override
    public DataModel format(ScrapeResult scrapeResult) {
        return formatService.format(scrapeResult);
    }

    @Override
    public ProcessResult process(DataModel dataModel) {
        return null;
    }

    @Override
    public InformationPiece format(ProcessResult processResult) {
        return formatService.format(processResult);
    }

    @Override
    public InformationPiece save(InformationPiece informationPiece) {
        return persistenceService.save(informationPiece);
    }

    @Override
    public void processAndSave(DataModel dataModel) {
        processingOrchestrationService.orchestrate(dataModel);
    }

}
