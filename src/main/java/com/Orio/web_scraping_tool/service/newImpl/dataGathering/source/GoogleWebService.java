package com.Orio.web_scraping_tool.service.newImpl.dataGathering.source;

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
import com.Orio.web_scraping_tool.service.dataGathering.search.IWebSearchService;
import com.Orio.web_scraping_tool.service.dataGathering.source.IDataSource;
import com.Orio.web_scraping_tool.service.newImpl.dataGathering.search.GoogleSearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleWebService implements IDataSource {

    private final List<IWebSearchService> searchEngines;
    private final IScrapeService scrapeService;
    private static final Logger logger = LoggerFactory.getLogger(GoogleWebService.class);

    @Override
    public List<DataModel> getData(String query) throws DataSourceUnavailableException { // TODO handle null and empty

        if (searchEngines.isEmpty()) {
            throw new DataSourceUnavailableException("No web search engine was found");
        }

        List<String> links = new ArrayList<>();

        IWebSearchService googleSearchEngine = searchEngines.stream()
                .filter(searchEngine -> searchEngine instanceof GoogleSearchService).findFirst().get();

        searchEngines.remove(searchEngines.indexOf(googleSearchEngine));
        searchEngines.add(0, googleSearchEngine);
        int i = 0;

        do {
            IWebSearchService searchEngine = searchEngines.get(i++);
            links.addAll(this.getLinksFromSearchEngine(searchEngine, query));
        } while (links.isEmpty() && i < searchEngines.size());

        if (links.isEmpty()) {
            throw new DataSourceUnavailableException("No web search engine worked and there are no links found");
        }

        return scrapeService.scrape(links);
    }

    private List<String> getLinksFromSearchEngine(IWebSearchService searchEngine, String query) {
        try {
            return searchEngine.getLinks(query);
        } catch (RateLimitReachedException | WebSearchException e) {
            logger.error("Exception occurred while reading from search engine: {}. Exception: {}", searchEngine, e);
            return new ArrayList<>();
        }
    }

}
