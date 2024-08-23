package com.Orio.web_scraping_tool.service;

import java.util.List;

import com.Orio.web_scraping_tool.model.DataModel;

public interface IQueryHandlerService {

    List<DataModel> getData(String query);

    void processData(List<DataModel> data);

    void saveData(List<DataModel> data);

}
