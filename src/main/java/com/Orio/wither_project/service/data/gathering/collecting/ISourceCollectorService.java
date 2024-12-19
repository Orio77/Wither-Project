package com.Orio.wither_project.service.data.gathering.collecting;

import java.util.List;

import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.data.source.IDataSource;

public interface ISourceCollectorService {

    List<DataModel> collectData(List<IDataSource> dataSources, String query);
}
