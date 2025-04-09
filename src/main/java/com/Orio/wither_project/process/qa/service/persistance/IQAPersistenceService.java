package com.Orio.wither_project.process.qa.service.persistance;

import java.util.List;

import com.Orio.wither_project.process.qa.model.QAModel;

public interface IQAPersistenceService {

    void save(QAModel qaModel);

    void save(List<QAModel> qaModels);
}
