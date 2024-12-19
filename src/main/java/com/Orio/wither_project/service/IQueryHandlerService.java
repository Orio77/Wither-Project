package com.Orio.wither_project.service;

import java.util.List;

import com.Orio.wither_project.model.DataModel;

public interface IQueryHandlerService {

    List<DataModel> getData(String query);

    void processData(List<DataModel> data);

    void saveData(List<DataModel> data);

}
