package com.Orio.wither_project.query.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.wither_project.core.constants.ApiPaths;
import com.Orio.wither_project.gather.exception.InvalidQueryException;
import com.Orio.wither_project.process.qa.model.QAModel;
import com.Orio.wither_project.query.exception.EmptyVectorDatabaseException;
import com.Orio.wither_project.query.service.IQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.BASE)
@Slf4j
public class QueryController {

    private final IQueryService queryService;

    @GetMapping(ApiPaths.QUERY)
    public ResponseEntity<?> query(@RequestParam(required = false) String query) {

        try {
            List<QAModel> results = queryService.run(query);
            return ResponseEntity.ok(results);
        } catch (InvalidQueryException e) {
            log.error("Invalid query parameter: {}", query);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EmptyVectorDatabaseException e) {
            log.error("Vector database is empty, please gather the data first");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while processing the query: {}", query, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the query");
        }
    }
}
