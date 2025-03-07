package com.Orio.wither_project.gader.service.process.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gader.model.DataModel;
import com.Orio.wither_project.gader.model.ProcessResult;
import com.Orio.wither_project.gader.service.process.IProcessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicProcessService implements IProcessService {

    private static final Logger logger = LoggerFactory.getLogger(BasicProcessService.class);

    @Override
    public ProcessResult process(DataModel dataModel) {
        logger.info("Processing data model: " + dataModel);
        return new ProcessResult();
    }

}
