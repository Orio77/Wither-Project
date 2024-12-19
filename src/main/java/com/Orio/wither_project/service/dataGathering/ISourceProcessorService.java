package com.Orio.wither_project.service.dataGathering;

import java.util.List;

import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.dataGathering.source.IDataSource;

public interface ISourceProcessorService {

    List<DataModel> collectData(List<IDataSource> dataSources, String query);
}
