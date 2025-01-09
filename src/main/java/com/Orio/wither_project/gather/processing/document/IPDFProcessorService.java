package com.Orio.wither_project.gather.processing.document;

import java.util.List;

import com.Orio.wither_project.model.DataModel;

public interface IPDFProcessorService {

    // Uses the sql db to retrieve files

    List<DataModel> getData(String query);
}
