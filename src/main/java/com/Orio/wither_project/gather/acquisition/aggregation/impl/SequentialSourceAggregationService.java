package com.Orio.wither_project.gather.acquisition.aggregation.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.exception.DataSourceUnavailableException;
import com.Orio.wither_project.gather.acquisition.aggregation.ISourceAggregationService;
import com.Orio.wither_project.gather.acquisition.source.IDataSource;
import com.Orio.wither_project.model.DataModel;

@Service
public class SequentialSourceAggregationService implements ISourceAggregationService {

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
