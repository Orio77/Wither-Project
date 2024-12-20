package com.Orio.wither_project.service.data.managing.repoService;

import java.util.List;

import com.Orio.wither_project.exception.UnauthorizedException;
import com.Orio.wither_project.model.DataModel;

public interface ISQLDataModelService {

    void save(List<DataModel> data);

    List<DataModel> get(List<Long> ids);

    List<DataModel> findByQuestionIn(List<String> questions);

    List<DataModel> findByQuestionInIgnoreCase(List<String> questions);

    void remove(List<Long> ids, String removePassword) throws UnauthorizedException;
}
