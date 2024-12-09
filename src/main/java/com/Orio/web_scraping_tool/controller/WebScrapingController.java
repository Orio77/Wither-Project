package com.Orio.web_scraping_tool.controller;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Orio.web_scraping_tool.model.DataModel;
import com.Orio.web_scraping_tool.service.IQueryHandlerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WebScrapingController {
    private static final Logger logger = getLogger(WebScrapingController.class);

    private final IQueryHandlerService queryHandlerService;

    @GetMapping("/gather")
    public ResponseEntity<?> gatherData(@RequestParam(required = true) String query) {
        try {
            List<DataModel> data = queryHandlerService.getData(query);
            if (data.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            queryHandlerService.processData(data);
            queryHandlerService.saveData(data);

            return ResponseEntity.ok(data);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error gathering data for query: " + query, e);
            return ResponseEntity.internalServerError()
                    .body("An error occurred while processing your request");
        }
    }

}
