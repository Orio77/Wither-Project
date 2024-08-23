package com.Orio.web_scraping_tool.service.dataGathering;

import java.util.List;

import com.Orio.web_scraping_tool.model.DataModel;

public interface IPDFProcessorService {

    // (Enters the /downloaded/{query} dir, and for each file saves the content)
    List<DataModel> getData(String query);
}
