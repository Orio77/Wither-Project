package com.Orio.wither_project.gader.service.search.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gader.model.SearchResult;
import com.Orio.wither_project.gader.service.search.ISearchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicSearchService implements ISearchService {

    private static final Logger logger = LoggerFactory.getLogger(BasicSearchService.class);

    @Override
    public SearchResult search(String query) {
        logger.info("Searching for query: {}", query);
        return new SearchResult();
    }

}
