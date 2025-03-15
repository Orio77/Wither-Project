package com.Orio.wither_project.gather.service.persist.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.service.persist.IPersistenceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicPersistenceService implements IPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(BasicPersistenceService.class);

    @Override
    public InformationPiece save(InformationPiece informationPiece) {
        logger.info("Saving information piece: " + informationPiece);
        return informationPiece;
    }

}
