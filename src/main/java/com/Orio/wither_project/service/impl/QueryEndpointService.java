package com.Orio.wither_project.service.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.Orio.wither_project.exception.InvalidQueryException;
import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.data.managing.handling.IQueryService;
import com.Orio.wither_project.service.data.managing.repoService.ISQLDataModelService;
import com.Orio.wither_project.service.data.managing.repoService.IVectorStoreService;

import lombok.RequiredArgsConstructor;

/**
 * Service class handling data queries using vector store and SQL services.
 */
@Service
@RequiredArgsConstructor
public class QueryEndpointService implements IQueryService {
    private static final Logger logger = getLogger(QueryEndpointService.class);
    private static final int MAX_RESULTS = 100;

    private final IVectorStoreService vectorStoreService;
    private final ISQLDataModelService sqlService;

    @Override
    public List<DataModel> handle(String question, int numResults) {
        validateInput(question, numResults);

        logger.info("Querying data with question: '{}', numResults: {}", question, numResults);

        try {
            List<String> resultQuestions = vectorStoreService.search(question, numResults);
            logger.debug("Vector store returned questions: {}", resultQuestions);

            List<DataModel> data = sqlService.findByQuestionIn(resultQuestions);

            if (data.isEmpty()) {
                logger.debug("No matches found in SQL database for questions: {}", resultQuestions);
            }

            logger.debug("Found {} results for query", data.size());
            return data;
        } catch (Exception e) {
            logger.error("Error processing query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process query", e);
        }
    }

    private void validateInput(String question, int numResults) {
        if (!StringUtils.hasText(question)) {
            throw new InvalidQueryException("Question cannot be empty");
        }
        if (numResults <= 0 || numResults > MAX_RESULTS) {
            throw new InvalidQueryException("Number of results must be between 1 and " + MAX_RESULTS);
        }
    }
}
