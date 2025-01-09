package com.Orio.wither_project.gather.acquisition.aggregation;

import java.util.List;

import com.Orio.wither_project.gather.acquisition.source.IDataSource;
import com.Orio.wither_project.model.DataModel;

public interface ISourceAggregationService {

    List<DataModel> collectData(List<IDataSource> dataSources, String query);
}
