package com.Orio.wither_project.query.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.exception.InvalidQueryException;
import com.Orio.wither_project.gather.service.persist.IVectorStoreService;
import com.Orio.wither_project.process.qa.model.QAModel;
import com.Orio.wither_project.query.exception.EmptyVectorDatabaseException;
import com.Orio.wither_project.query.repository.QAModelRepo;
import com.Orio.wither_project.query.service.IQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService implements IQueryService {

    private final QAModelRepo qaModelRepo;
    private final IVectorStoreService vectorStoreService;

    @Override
    public List<String> getQuestions(String query) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("Query parameter is empty or missing: {}", query);
            throw new InvalidQueryException("Query parameter is required and cannot be empty");
        }

        log.info("Retrieving questions for query: {}", query);
        List<String> questions = vectorStoreService.getQuestions(query);
        log.info("Retrieved {} questions for query", questions.size());
        log.debug("Retrieved questions: {}", questions);
        return questions;
    }

    @Override
    public List<QAModel> getQAModels(List<String> questions) {
        log.info("Fetching QA models for {} questions", questions.size());
        log.debug("Questions to fetch: {}", questions);
        List<QAModel> models = qaModelRepo.findByQuestionIn(questions);
        log.info("Retrieved {} QA models", models.size());
        if (models.isEmpty()) {
            log.warn("No QA models found for the provided questions");
            throw new EmptyVectorDatabaseException("Vector database is empty, please gather the data first");
        }
        return models;
    }
}
