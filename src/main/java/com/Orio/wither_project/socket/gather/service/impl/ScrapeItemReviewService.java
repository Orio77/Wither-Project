
package com.Orio.wither_project.socket.gather.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.model.dto.ScrapeItemReviewRequestDTO;
import com.Orio.wither_project.gather.model.dto.ScrapeItemReviewResultDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScrapeItemReviewService {
    private static final String REVIEW_TOPIC = "/topic/review/scrape-items";
    private static final long DEFAULT_TIMEOUT_SECONDS = 300; // 5 minutes timeout

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, CompletableFuture<List<ScrapeItem>>> pendingReviews = new ConcurrentHashMap<>();

    /**
     * Sends scrape items to the frontend for review and waits for response
     * 
     * @param scrapeItems The scrape items to be reviewed
     * @return The accepted data items
     */
    public List<ScrapeItem> sendForReviewAndWait(List<ScrapeItem> scrapeItems) {
        String reviewId = UUID.randomUUID().toString();
        log.info("Starting review process with ID: {}", reviewId);

        // Create a future to be completed when the frontend responds
        CompletableFuture<List<ScrapeItem>> future = new CompletableFuture<>();
        pendingReviews.put(reviewId, future);

        // Send the data to the frontend
        ScrapeItemReviewRequestDTO reviewDTO = new ScrapeItemReviewRequestDTO(reviewId, scrapeItems);
        messagingTemplate.convertAndSend(REVIEW_TOPIC, reviewDTO);
        log.info("Sent {} scrape items for review with ID: {}", scrapeItems.size(), reviewId);

        try {
            // Wait for the frontend to respond with accepted items
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
     * @param result The review result containing accepted items
     */
    public void completeReview(ScrapeItemReviewResultDTO result) {
        String reviewId = result.getReviewId();
        log.info("Received review result for ID: {}", reviewId);

        CompletableFuture<List<ScrapeItem>> pendingFuture = pendingReviews.remove(reviewId);
        if (pendingFuture != null) {
            pendingFuture.complete(result.getAcceptedScrapeItems());
            log.info("Completed review process for ID: {}, accepted {} items",
                    reviewId, result.getAcceptedScrapeItems().size());
        } else {
            log.warn("Received results for unknown or expired review ID: {}", reviewId);
        }
    }
}
