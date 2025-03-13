package com.Orio.wither_project.gader.service.process;

import java.util.List;

import com.Orio.wither_project.gader.model.QAModel;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IQAService {

    List<QAModel> extract(String content) throws JsonProcessingException;
}
