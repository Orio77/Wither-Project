package com.Orio.wither_project.service.data.gathering.scraping;

import java.util.List;

import com.Orio.wither_project.model.DataModel;

public interface IScrapeService {

    List<DataModel> scrape(List<String> links);
}
