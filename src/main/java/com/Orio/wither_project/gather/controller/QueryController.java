package com.Orio.wither_project.gather.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.wither_project.core.constants.ApiPaths;
import com.Orio.wither_project.gather.model.QAModel;
import com.Orio.wither_project.gather.repository.QAModelRepo;
import com.Orio.wither_project.gather.service.persist.impl.Neo4jVectorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.BASE)
@Slf4j
public class QueryController {

    private final Neo4jVectorService neo4jVectorService;
    private final QAModelRepo qaModelRepo;

    @GetMapping(ApiPaths.QUERY)
    public ResponseEntity<?> query(@RequestParam(required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("Query parameter is empty or missing");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Query parameter is required and cannot be empty");
        }

        try {
            List<String> questions = neo4jVectorService.getQuestions(query);
            log.info("Found {} questions for query: {}", questions.size(), query);
            List<QAModel> results = qaModelRepo.findByQuestionIn(questions);
            log.info("Found {} results for query: {}", results.size(), query);

            if (results.isEmpty()) {
                log.info("No results found for query: {}", query);
                return ResponseEntity.ok(Collections.emptyList());
            }

            log.info("Successfully retrieved results for query: {}", query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error processing query: {}", query, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing your query: " + e.getMessage());
        }
    }

}
