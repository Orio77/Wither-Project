package com.Orio.web_scraping_tool.service.impl.dataGathering.source;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.dataGathering.source.IDataSource;

@Service
public class CoreService implements IDataSource {

    @Override
    public List<DataModel> getData(String query) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getData'");
    }

}
