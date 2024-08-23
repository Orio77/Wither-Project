package com.Orio.web_scraping_tool.service.dataGathering.search;

import java.util.List;

import com.Orio.web_scraping_tool.exception.RateLimitReachedException;
import com.Orio.web_scraping_tool.exception.WebSearchException;

public interface IWebSearchService {

    List<String> getLinks(String query) throws RateLimitReachedException, WebSearchException;
}
