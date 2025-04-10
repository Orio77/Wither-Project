package com.Orio.wither_project.process.qa.service.persistance.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.service.persist.impl.Neo4jVectorService;
import com.Orio.wither_project.process.qa.model.QAModel;
import com.Orio.wither_project.process.qa.service.persistance.IQAPersistenceService;
import com.Orio.wither_project.query.repository.QAModelRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostgresNeo4jQAPersistenceService implements IQAPersistenceService {

    private final QAModelRepo qaModelRepo;
    private final Neo4jVectorService vectorService;

    @Override
    public void save(QAModel qaModel) {
        log.info("Saving QA model with question: '{}', answer: '{}'..., and source: '{}'", qaModel.getQuestion(),
                qaModel.getAnswer().substring(0, 100), qaModel.getSource());
        qaModelRepo.save(qaModel);
        log.debug("QA model saved to PostgreSQL");

        vectorService.save(qaModel.getQuestion());
        log.debug("Question vector saved to Neo4j");
    }

    @Override
    public void save(List<QAModel> qaModels) {
        log.info("Saving batch of {} QA models", qaModels.size());
        qaModelRepo.saveAll(qaModels);
        log.debug("QA models batch saved to PostgreSQL");

        List<String> questions = qaModels.stream().map(QAModel::getQuestion).toList();
        vectorService.save(questions);
        log.debug("Question vectors batch saved to Neo4j");
    }
}
