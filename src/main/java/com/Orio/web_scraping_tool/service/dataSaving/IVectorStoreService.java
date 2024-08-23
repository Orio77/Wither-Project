package com.Orio.web_scraping_tool.service.dataSaving;

import java.util.List;

import com.Orio.web_scraping_tool.exception.UnauthorizedException;
import com.Orio.web_scraping_tool.model.DataModel;

public interface IVectorStoreService {

    void save(List<DataModel> questions);

    List<String> search(String question);

    boolean remove(List<String> elementId, String removePassword) throws UnauthorizedException;
}
