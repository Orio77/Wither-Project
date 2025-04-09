package com.Orio.wither_project.query.service;

import java.util.List;

import com.Orio.wither_project.process.qa.model.QAModel;

public interface IQueryService {

    default List<QAModel> run(String query) {
        List<String> questions = getQuestions(query);
        List<QAModel> result = getQAModels(questions);
        return result;
    }

    List<String> getQuestions(String query);

    List<QAModel> getQAModels(List<String> questions);
}
