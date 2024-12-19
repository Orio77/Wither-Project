package com.Orio.wither_project.service.dataGathering;

import java.util.List;

import com.Orio.wither_project.model.DataModel;

public interface IPDFProcessorService {

    // (Enters the /downloaded/{query} dir, and for each file saves the content)
    List<DataModel> getData(String query);
}
