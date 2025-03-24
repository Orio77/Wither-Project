
package com.Orio.wither_project.socket.gather.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.dto.DataModelReviewRequestDTO;
import com.Orio.wither_project.gather.model.dto.DataModelReviewResultDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataModelReviewService {
    private static final String REVIEW_TOPIC = "/topic/review/data-models";
    private static final long DEFAULT_TIMEOUT_SECONDS = 300; // 5 minutes timeout

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, CompletableFuture<List<DataModel>>> pendingReviews = new ConcurrentHashMap<>();

    /**
     * Sends data models to the frontend for review and waits for response
     * 
     * @param dataModels The data models to be reviewed
     * @return The accepted data models
     */
    public List<DataModel> sendForReviewAndWait(List<DataModel> dataModels) {
        String reviewId = UUID.randomUUID().toString();
        log.info("Starting review process with ID: {}", reviewId);

        // Create a future to be completed when the frontend responds
        CompletableFuture<List<DataModel>> future = new CompletableFuture<>();
        pendingReviews.put(reviewId, future);

        // Send the data to the frontend
        DataModelReviewRequestDTO reviewDTO = new DataModelReviewRequestDTO(reviewId, dataModels);
        messagingTemplate.convertAndSend(REVIEW_TOPIC, reviewDTO);
        log.info("Sent {} data models for review with ID: {}", dataModels.size(), reviewId);

        try {
            // Wait for the frontend to respond with accepted models
            return future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error waiting for review completion: {}", e.getMessage(), e);
            pendingReviews.remove(reviewId);
            throw new RuntimeException("Review process failed or timed out", e);
        }
    }

    /**
     * Process review results received from the frontend
     * 
     * @param result The review result containing accepted models
     */
    public void completeReview(DataModelReviewResultDTO result) {
        String reviewId = result.getReviewId();
        log.info("Received review result for ID: {}", reviewId);

        CompletableFuture<List<DataModel>> pendingFuture = pendingReviews.remove(reviewId);
        if (pendingFuture != null) {
            pendingFuture.complete(result.getAcceptedModels());
            log.info("Completed review process for ID: {}, accepted {} models",
                    reviewId, result.getAcceptedModels().size());
        } else {
            log.warn("Received results for unknown or expired review ID: {}", reviewId);
        }
    }
}
