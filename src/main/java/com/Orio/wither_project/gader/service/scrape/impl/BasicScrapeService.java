package com.Orio.wither_project.gader.service.scrape.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gader.model.DataSource;
import com.Orio.wither_project.gader.model.ScrapeResult;
import com.Orio.wither_project.gader.service.scrape.IScrapeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicScrapeService implements IScrapeService {

    private static final Logger logger = LoggerFactory.getLogger(BasicScrapeService.class);

    @Override
    public ScrapeResult scrape(DataSource dataSource) {
        logger.info("Scraping data from " + dataSource);

        return new ScrapeResult();
    }

}
