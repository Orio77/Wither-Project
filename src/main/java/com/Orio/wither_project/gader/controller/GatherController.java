package com.Orio.wither_project.gader.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.wither_project.constants.ApiPaths;
import com.Orio.wither_project.gader.model.InformationPiece;
import com.Orio.wither_project.gader.model.dto.QueryRequest;
import com.Orio.wither_project.gader.service.orchestration.IWitherOrchestrationService;

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

    /**
     * Processes a query and returns gathered information.
     *
     * @param request The query request containing the search parameters
     * @return ResponseEntity with the gathered information
     */
    @PostMapping(value = ApiPaths.GATHER, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InformationPiece> gatherInformation(@RequestBody QueryRequest request) {
        logger.info("Processing gather request with query: {}", request.getQuery());

        try {
            InformationPiece informationPiece = witherOrchestrationService.orchestrate(request.getQuery());
            return ResponseEntity.ok(informationPiece);
        } catch (Exception e) {
            logger.error("Error processing gather request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}