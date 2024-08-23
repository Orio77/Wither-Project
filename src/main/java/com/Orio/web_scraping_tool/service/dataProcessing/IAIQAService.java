package com.Orio.web_scraping_tool.service.dataProcessing;

import java.util.List;

import com.Orio.web_scraping_tool.model.DataModel;

public interface IAIQAService {

    public void generateQuestions(List<DataModel> data);

    public void generateAnswers(List<DataModel> data);
}
