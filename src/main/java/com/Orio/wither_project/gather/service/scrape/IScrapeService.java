package com.Orio.wither_project.gather.service.scrape;

import com.Orio.wither_project.gather.model.DataSource;
import com.Orio.wither_project.gather.model.ScrapeResult;

public interface IScrapeService {

    ScrapeResult scrape(DataSource dataSource);
}
