package com.Orio.wither_project.service.impl.dataGathering.source;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.dataGathering.source.IDataSource;

@Service
public class YouTubeService implements IDataSource {

    @Override
    public List<DataModel> getData(String query) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getData'");
    }

}