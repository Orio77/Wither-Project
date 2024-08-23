package com.Orio.web_scraping_tool.service.dataGathering;

import java.util.List;

import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.dataGathering.source.IDataSource;

public interface ISourceProcessorService {

    List<DataModel> collectData(List<IDataSource> dataSources, String query);
}
