package com.Orio.wither_project.gather.storage.vector.service;

import java.util.List;

import com.Orio.wither_project.exception.UnauthorizedException;
import com.Orio.wither_project.model.DataModel;

public interface IVectorStoreService {

    void save(List<DataModel> questions);

    List<String> search(String question);

    List<String> search(String question, int topK);

    boolean remove(List<String> elementId, String removePassword) throws UnauthorizedException;
}
