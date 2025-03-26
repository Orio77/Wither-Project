package com.Orio.wither_project.gather.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.wither_project.core.constants.ApiPaths;
import com.Orio.wither_project.gather.model.dto.QueryRequest;
import com.Orio.wither_project.gather.service.orchestration.INewWitherOrchestrationService;
import com.Orio.wither_project.gather.service.orchestration.IWitherOrchestrationService;

import lombok.RequiredArgsConstructor;

/**
 * Controller responsible for handling information gathering requests.
 */
@RestController
@RequestMapping(ApiPaths.BASE)
@RequiredArgsConstructor
public class GatherController {

    private static final Logger logger = LoggerFactory.getLogger(GatherController.class);

    private final IWitherOrchestrationService witherOrchestrationService;
    private final INewWitherOrchestrationService newWitherOrchestrationService;

    /**
     * Processes a query and returns gathered information.
     *
     * @param request The query request containing the search parameters
     * @return ResponseEntity with the gathered information or appropriate error
     *         response
     */
    @PostMapping(value = ApiPaths.GATHER, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> gatherInformation(@RequestBody QueryRequest request) {
        // Check if request is null
        if (request == null) {
            logger.warn("Received null request");
            return ResponseEntity.badRequest().build();
        }

        // Check if query is null or empty
        String query = request.getQuery();
        if (!StringUtils.hasText(query)) {
            logger.warn("Received request with empty or null query");
            return ResponseEntity.badRequest().build();
        }

        logger.info("Processing gather request with query: {}", query);

        try {
            witherOrchestrationService.orchestrate(query);
            return ResponseEntity.ok().build();
        } catch (NullPointerException e) {
            logger.error("Null pointer exception processing gather request", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error processing gather request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/v2"
            + ApiPaths.GATHER, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> gatherData(@RequestBody QueryRequest request) {
        // Check if request is null
        if (request == null) {
            logger.warn("Received null request");
            return ResponseEntity.badRequest().build();
        }

        // Check if query is null or empty
        String query = request.getQuery();
        if (!StringUtils.hasText(query)) {
            logger.warn("Received request with empty or null query");
            return ResponseEntity.badRequest().build();
        }

        logger.info("Processing gather request with query: {}", query);

        try {
            newWitherOrchestrationService.gather(query);
            return ResponseEntity.ok().build();
        } catch (NullPointerException e) {
            logger.error("Null pointer exception processing gather request", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error processing gather request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}