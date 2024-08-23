package com.Orio.web_scraping_tool.service.newImpl.dataGathering;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.web_scraping_tool.exception.DataSourceUnavailableException;
import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.dataGathering.ISourceProcessorService;
import com.Orio.web_scraping_tool.service.dataGathering.source.IDataSource;

@Service
public class SequentialSourceProcessorService implements ISourceProcessorService {

    @Override
    public List<DataModel> collectData(List<IDataSource> dataSources, String query) { // TODO handle nulls and empty's
        List<DataModel> data = new ArrayList<>();

        dataSources.forEach(dataPiece -> {
            List<DataModel> result = null;
            try {
                result = dataPiece.getData(query);
            } catch (DataSourceUnavailableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } // TODO handle nulls and empty's
            data.addAll(result);
        });

        return data;
    }

}
