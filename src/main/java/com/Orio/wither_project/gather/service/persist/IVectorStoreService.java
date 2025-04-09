package com.Orio.wither_project.gather.service.persist;

import java.util.List;

public interface IVectorStoreService {

    List<String> getQuestions(String question);

    void save(List<String> questions);

    void save(String question);
}
