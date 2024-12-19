package com.Orio.wither_project.service.dataGathering.webSearch;

import java.util.List;

import com.Orio.wither_project.exception.RateLimitReachedException;
import com.Orio.wither_project.exception.WebSearchException;

public interface IWebSearchService {

    List<String> getLinks(String query) throws RateLimitReachedException, WebSearchException;
}
