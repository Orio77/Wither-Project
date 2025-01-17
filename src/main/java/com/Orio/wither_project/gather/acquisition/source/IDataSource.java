package com.Orio.wither_project.gather.acquisition.source;

import java.util.List;

import com.Orio.wither_project.exception.DataSourceUnavailableException;
import com.Orio.wither_project.model.DataModel;

public interface IDataSource {

    List<DataModel> getData(String query) throws DataSourceUnavailableException;
}
