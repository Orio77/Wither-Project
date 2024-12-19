package com.Orio.wither_project.service.data.source.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.exception.DataSourceUnavailableException;
import com.Orio.wither_project.exception.RateLimitReachedException;
import com.Orio.wither_project.exception.WebSearchException;
import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.data.gathering.scraping.IScrapeService;
import com.Orio.wither_project.service.data.gathering.searching.IWebSearchService;
import com.Orio.wither_project.service.data.source.IDataSource;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleWebDataSource implements IDataSource {

    private final List<IWebSearchService> webSearchServices;
    private final IScrapeService scrapeService;
    private static final Logger logger = LoggerFactory.getLogger(GoogleWebDataSource.class);

    @Override
    public List<DataModel> getData(String query) throws DataSourceUnavailableException { // TODO handle null and empty

        List<String> links = webSearchServices.stream()
                .map(service -> getLinksFromSearchService(service, query))
                .flatMap(List::stream)
                .toList();

        if (links.isEmpty()) {
            throw new DataSourceUnavailableException("No web search engine worked and there are no links found");
        }

        return scrapeService.scrape(links);
    }

    private List<String> getLinksFromSearchService(IWebSearchService webSearchService, String query) {
        try {
            return webSearchService.getLinks(query);
        } catch (RateLimitReachedException | WebSearchException e) {
            logger.error("Exception occurred while reading from search engine: {}. Exception: {}", webSearchService, e);
            return new ArrayList<>();
        }
    }

}
