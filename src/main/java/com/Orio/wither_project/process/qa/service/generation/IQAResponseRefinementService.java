package com.Orio.wither_project.process.qa.service.generation;

import com.Orio.wither_project.process.qa.model.QAModel;

public interface IQAResponseRefinementService {

    QAModel refine(QAModel qaModel, String content);
}
