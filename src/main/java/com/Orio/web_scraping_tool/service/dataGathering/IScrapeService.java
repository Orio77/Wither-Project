package com.Orio.web_scraping_tool.service.dataGathering;

import java.util.List;

import com.Orio.web_scraping_tool.model.DataModel;

public interface IScrapeService {

    List<DataModel> scrape(List<String> links);
}
