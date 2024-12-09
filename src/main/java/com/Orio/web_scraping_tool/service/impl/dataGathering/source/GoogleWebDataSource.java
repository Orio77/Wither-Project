package com.Orio.web_scraping_tool.service.impl.dataGathering.source;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.web_scraping_tool.exception.DataSourceUnavailableException;
import com.Orio.web_scraping_tool.exception.RateLimitReachedException;
import com.Orio.web_scraping_tool.exception.WebSearchException;
import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.dataGathering.IScrapeService;
import com.Orio.web_scraping_tool.service.dataGathering.source.IDataSource;
import com.Orio.web_scraping_tool.service.dataGathering.webSearch.IWebSearchService;
import com.Orio.web_scraping_tool.service.impl.dataGathering.search.GoogleSearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleWebDataSource implements IDataSource {

    private final GoogleSearchService googleSearchService;
    private final IScrapeService scrapeService;
    private static final Logger logger = LoggerFactory.getLogger(GoogleWebDataSource.class);

    @Override
    public List<DataModel> getData(String query) throws DataSourceUnavailableException { // TODO handle null and empty

        List<String> links = getLinksFromSearchService(googleSearchService, query);

        if (links.isEmpty()) {
            throw new DataSourceUnavailableException("No web search engine worked and there are no links found");
        }

        return scrapeService.scrape(links);
    }

    private List<String> getLinksFromSearchService(IWebSearchService searchService, String query) {
        try {
            return searchService.getLinks(query);
        } catch (RateLimitReachedException | WebSearchException e) {
            logger.error("Exception occurred while reading from search engine: {}. Exception: {}", searchService, e);
            return new ArrayList<>();
        }
    }

}
