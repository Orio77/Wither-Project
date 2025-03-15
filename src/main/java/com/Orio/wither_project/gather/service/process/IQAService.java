package com.Orio.wither_project.gather.service.process;

import java.util.List;

import com.Orio.wither_project.gather.model.QAModel;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface IQAService {

    List<QAModel> extract(String content) throws JsonProcessingException;
}
