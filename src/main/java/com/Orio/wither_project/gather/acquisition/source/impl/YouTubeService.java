package com.Orio.wither_project.gather.acquisition.source.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.acquisition.source.IDataSource;
import com.Orio.wither_project.model.DataModel;

@Service
public class YouTubeService implements IDataSource {

    @Override
    public List<DataModel> getData(String query) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getData'");
    }

}
