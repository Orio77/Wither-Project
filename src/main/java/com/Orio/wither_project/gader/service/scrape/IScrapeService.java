package com.Orio.wither_project.gader.service.scrape;

import com.Orio.wither_project.gader.model.DataSource;
import com.Orio.wither_project.gader.model.ScrapeResult;

public interface IScrapeService {

    ScrapeResult scrape(DataSource dataSource);
}
