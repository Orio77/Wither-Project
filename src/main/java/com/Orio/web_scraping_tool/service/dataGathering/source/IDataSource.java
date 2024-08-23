package com.Orio.web_scraping_tool.service.dataGathering.source;

import java.util.List;

import com.Orio.web_scraping_tool.exception.DataSourceUnavailableException;
import com.Orio.web_scraping_tool.model.DataModel;

public interface IDataSource {

    List<DataModel> getData(String query) throws DataSourceUnavailableException;
}
