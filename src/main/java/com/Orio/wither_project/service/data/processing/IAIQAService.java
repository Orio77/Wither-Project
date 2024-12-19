package com.Orio.wither_project.service.data.processing;

import java.util.List;

import com.Orio.wither_project.model.DataModel;

public interface IAIQAService {

    public void generateQuestions(List<DataModel> data);

    public void generateAnswers(List<DataModel> data);
}
