package com.Orio.web_scraping_tool.service.dataSaving;

import java.util.List;

import com.Orio.web_scraping_tool.exception.UnauthorizedException;
import com.Orio.web_scraping_tool.model.DataModel;

public interface ISQLService {

    void save(List<DataModel> data);

    List<DataModel> get(List<Long> ids);

    void remove(List<Long> ids, String removePassword) throws UnauthorizedException;
}
