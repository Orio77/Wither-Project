package com.Orio.wither_project.service.data.source.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.data.source.IDataSource;

@Service
public class CoreService implements IDataSource {

    @Override
    public List<DataModel> getData(String query) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getData'");
    }

}
