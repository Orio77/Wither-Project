package com.Orio.wither_project.service.data.managing.handling;

import java.util.List;

import com.Orio.wither_project.model.DataModel;

public interface IQueryService {

    List<DataModel> handle(String query, int numQuestions);
}
