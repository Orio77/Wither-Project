package com.Orio.wither_project.gader.service.format.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gader.model.DataModel;
import com.Orio.wither_project.gader.model.DataSource;
import com.Orio.wither_project.gader.model.InformationPiece;
import com.Orio.wither_project.gader.model.ProcessResult;
import com.Orio.wither_project.gader.model.ScrapeResult;
import com.Orio.wither_project.gader.model.SearchResult;
import com.Orio.wither_project.gader.service.format.IFormatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicFormatService implements IFormatService {

    private static Logger logger = LoggerFactory.getLogger(BasicFormatService.class);

    @Override
    public DataSource format(SearchResult searchResult) {
        logger.info("Formatting search result " + searchResult);
        return DataSource.builder().query(searchResult.getQuery()).items(searchResult.getItems())
                .errors(searchResult.getErrors()).build();
    }

    @Override
    public DataModel format(ScrapeResult scrapeResult) {
        return DataModel.builder().query(scrapeResult.getQuery()).items(scrapeResult.getItems())
                .errors(scrapeResult.getErrors()).build();
    }

    @Override
    public InformationPiece format(ProcessResult processResult) {
        logger.info("Formatting process result " + processResult);
        // Add logic for formatting processResult
        return new InformationPiece();
    }

}
