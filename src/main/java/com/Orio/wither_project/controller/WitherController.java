package com.Orio.wither_project.controller;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.IQueryHandlerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
public class WitherController {
    private static final Logger logger = getLogger(WitherController.class);
    private static final String GATHER_PATH = "/gather";

    private final IQueryHandlerService queryHandlerService;

    @Operation(summary = "Gather data based on query")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully gathered"),
            @ApiResponse(responseCode = "204", description = "No data found"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(GATHER_PATH)
    public ResponseEntity<?> gatherData(
            @RequestParam(required = true) String query) {
        try {
            List<DataModel> data = queryHandlerService.getData(query);
            if (data.isEmpty()) {
                logger.info("No data found for query: {}", query);
                return ResponseEntity.noContent().build();
            }

            queryHandlerService.processData(data);
            queryHandlerService.saveData(data);

            logger.info("Successfully gathered data for query: {}", query);
            return ResponseEntity.ok(data);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid query parameter: {}", query, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error gathering data for query: {}", query, e);
            return ResponseEntity.internalServerError()
                    .body("An error occurred while processing your request");
        }
    }

    @Operation(summary = "Home endpoint")
    @ApiResponse(responseCode = "200", description = "Welcome message")
    @GetMapping(value = { "", "/" })
    public ResponseEntity<String> home() {
        logger.debug("Home endpoint accessed");
        return ResponseEntity.ok("Welcome to the Wither Project!");
    }

}