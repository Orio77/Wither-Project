package com.Orio.wither_project.gader.service.process;

import com.Orio.wither_project.gader.model.DataModel;
import com.Orio.wither_project.gader.model.ProcessResult;

public interface IProcessService {

    ProcessResult process(DataModel dataModel);
}
