package com.Orio.wither_project.controller.gather;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.wither_project.constants.ApiPaths;
import com.Orio.wither_project.model.DataModel;
import com.Orio.wither_project.service.impl.GatherEndpointService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiPaths.BASE + ApiPaths.GATHER)
@RequiredArgsConstructor
public class DataGatherController {

    private static final Logger logger = getLogger(DataGatherController.class);
    private final GatherEndpointService gatherEndpointService;

    @Operation(summary = "Gather data based on query")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data successfully gathered"),
            @ApiResponse(responseCode = "204", description = "No data found"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping()
    public ResponseEntity<?> gatherData(
            @RequestParam(required = true) String query) {
        try {
            List<DataModel> data = gatherEndpointService.gatherData(query);
            if (data.isEmpty()) {
                logger.info("No data found for query: {}", query);
                return ResponseEntity.noContent().build();
            }

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
}
