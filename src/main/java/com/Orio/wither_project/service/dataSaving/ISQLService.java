package com.Orio.wither_project.service.dataSaving;

import java.util.List;

import com.Orio.wither_project.exception.UnauthorizedException;
import com.Orio.wither_project.model.DataModel;

public interface ISQLService {

    void save(List<DataModel> data);

    List<DataModel> get(List<Long> ids);

    void remove(List<Long> ids, String removePassword) throws UnauthorizedException;
}
