package com.Orio.wither_project.gather.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.constants.ApiPaths;
import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.dto.DataModelReviewResultDTO;
import com.Orio.wither_project.socket.gather.service.impl.DataModelReviewService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class DataModelReviewControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DataModelReviewService dataModelReviewService;

    @Test
    public void testCompleteReviewEndpoint() throws InterruptedException, ExecutionException {
        log.info("Starting controller integration test");

        // Create test data and review ID
        String reviewId = UUID.randomUUID().toString();
        List<DataModel> acceptedModels = createTestDataModels(2);
        log.debug("Created test review with ID: {} and {} accepted models", reviewId, acceptedModels.size());

        // Create a CompletableFuture that will be completed when the service processes
        // the response
        CompletableFuture<Boolean> reviewProcessed = new CompletableFuture<>();

        // Create a mock of the pending review that the service would have created
        log.debug("Injecting pending review into service");
        injectPendingReview(reviewId, reviewProcessed);

        // Create the review result DTO
        DataModelReviewResultDTO resultDTO = new DataModelReviewResultDTO();
        resultDTO.setReviewId(reviewId);
        resultDTO.setAcceptedModels(acceptedModels);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Send the HTTP request
        String url = "http://localhost:" + port + ApiPaths.BASE + ApiPaths.REVIEW_COMPLETE;
        log.info("Sending HTTP request to: {}", url);
        ResponseEntity<Void> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(resultDTO, headers),
                Void.class);

        // Verify HTTP status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        log.info("Received HTTP response with status: {}", response.getStatusCode());

        // Verify that the review was processed by the service
        Boolean processed = false;
        try {
            log.debug("Waiting for service to process the review");
            processed = reviewProcessed.get(5, TimeUnit.SECONDS);
            log.info("Review processed by service: {}", processed);
        } catch (TimeoutException e) {
            log.error("Timeout waiting for review to be processed", e);
        }
        assertTrue(processed, "Review should be processed by the service");
    }

    private void injectPendingReview(String reviewId, CompletableFuture<Boolean> reviewProcessed) {
        log.debug("Injecting pending review with ID: {}", reviewId);
        // Create a CompletableFuture for the models that will capture when it's
        // completed
        CompletableFuture<List<DataModel>> modelsFuture = new CompletableFuture<>();
        modelsFuture.thenRun(() -> {
            log.debug("Review completed, notifying test");
            reviewProcessed.complete(true);
        });

        // Use reflection to access the private pendingReviews map and insert our test
        // future
        try {
            java.lang.reflect.Field pendingReviewsField = DataModelReviewService.class
                    .getDeclaredField("pendingReviews");
            pendingReviewsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, CompletableFuture<List<DataModel>>> pendingReviews = (java.util.Map<String, CompletableFuture<List<DataModel>>>) pendingReviewsField
                    .get(dataModelReviewService);
            pendingReviews.put(reviewId, modelsFuture);
            log.debug("Successfully injected pending review");
        } catch (Exception e) {
            log.error("Failed to inject pending review", e);
            throw new RuntimeException("Failed to inject pending review", e);
        }
    }

    private List<DataModel> createTestDataModels(int count) {
        log.debug("Creating {} test data models", count);
        List<DataModel> models = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DataModel model = DataModel.builder().query("Test Model " + i).build();
            // Set other required fields as needed
            models.add(model);
        }
        return models;
    }
}
