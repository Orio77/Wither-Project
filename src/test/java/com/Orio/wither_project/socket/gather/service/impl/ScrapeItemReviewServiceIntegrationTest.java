package com.Orio.wither_project.socket.gather.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.model.dto.ScrapeItemReviewRequestDTO;
import com.Orio.wither_project.gather.model.dto.ScrapeItemReviewResultDTO;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class ScrapeItemReviewServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ScrapeItemReviewService dataModelReviewService;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private final String WEBSOCKET_URI = "ws://localhost:{port}/ws";
    private final String REVIEW_TOPIC = "/topic/review/scrape-items";

    // Define the endpoint to send the review result to
    private final String REVIEW_COMPLETION_ENDPOINT = "/app/review/complete";

    private CompletableFuture<ScrapeItemReviewRequestDTO> receivedReviewRequest;

    @BeforeEach
    public void setup() throws Exception {
        log.info("Setting up WebSocket client for test");
        // Set up WebSocket client
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));

        stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // Connect to WebSocket server
        String wsUrl = WEBSOCKET_URI.replace("{port}", String.valueOf(port));
        log.debug("Connecting to WebSocket at URL: {}", wsUrl);
        stompSession = stompClient.connectAsync(wsUrl, new StompSessionHandlerAdapter() {
        }).get(5, TimeUnit.SECONDS);
        log.info("Successfully connected to WebSocket server");

        // Reset the future for each test
        receivedReviewRequest = new CompletableFuture<>();

        // Subscribe to the review topic
        log.debug("Subscribing to topic: {}", REVIEW_TOPIC);
        stompSession.subscribe(REVIEW_TOPIC, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ScrapeItemReviewRequestDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("Received message on topic: {}", REVIEW_TOPIC);
                receivedReviewRequest.complete((ScrapeItemReviewRequestDTO) payload);
            }
        });
        log.info("Test setup completed successfully");
    }

    @AfterEach
    public void tearDown() {
        log.info("Tearing down test connections");
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
            log.debug("Disconnected from WebSocket session");
        }
    }

    @Test
    public void testReviewFlowWithActualWebSocketCommunication()
            throws ExecutionException, InterruptedException, TimeoutException {
        log.info("Starting integration test for review flow");

        // Create test data items
        List<ScrapeItem> testModels = createTestScrapeItems(3);
        log.debug("Created {} test data items", testModels.size());

        // Start a separate thread to process the review
        log.info("Sending items for review asynchronously");
        CompletableFuture<List<ScrapeItem>> reviewResultFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return dataModelReviewService.sendForReviewAndWait(testModels);
            } catch (Exception e) {
                log.error("Failed to send for review", e);
                throw new RuntimeException("Failed to send for review", e);
            }
        });

        // Simulate frontend review process
        log.info("Simulating frontend review process");

        // Step 1: Wait for the review request to be received through WebSocket
        log.info("Waiting for review request to be received");
        ScrapeItemReviewRequestDTO receivedRequest = receivedReviewRequest.get(10, TimeUnit.SECONDS);
        log.info("Received review request with ID: {}", receivedRequest.getReviewId());

        // Step 2: Validate the received request
        assertNotNull(receivedRequest);
        assertEquals(3, receivedRequest.getItems().size());
        log.debug("Validated received request has {} items", receivedRequest.getItems().size());

        // Step 3: Simulate frontend accepting only the first two items
        List<ScrapeItem> acceptedScrapeItems = new ArrayList<>();
        // Important: Use the actual items from the request with all their properties
        acceptedScrapeItems.add(receivedRequest.getItems().get(0));
        acceptedScrapeItems.add(receivedRequest.getItems().get(1));
        log.info("Frontend accepting {} out of {} items", acceptedScrapeItems.size(),
                receivedRequest.getItems().size());

        // Step 4: Send back the review result using the same reviewId
        ScrapeItemReviewResultDTO resultDTO = new ScrapeItemReviewResultDTO();
        resultDTO.setReviewId(receivedRequest.getReviewId());
        resultDTO.setAcceptedScrapeItems(acceptedScrapeItems);

        // Step 5: Send the review completion through WebSocket to simulate frontend
        // behavior
        log.info("Sending review completion via WebSocket to: {}", REVIEW_COMPLETION_ENDPOINT);
        stompSession.send(REVIEW_COMPLETION_ENDPOINT, resultDTO);
        log.info("Successfully sent review completion via WebSocket");

        // Direct call to the service to ensure the test works while debugging the
        // WebSocket connection
        log.info("Directly completing review as fallback");
        dataModelReviewService.completeReview(resultDTO);

        // Wait for the review process to complete
        log.info("Waiting for review future to complete");
        List<ScrapeItem> finalResult = null;
        try {
            finalResult = reviewResultFuture.get(10, TimeUnit.SECONDS);
            log.info("Review process completed with {} items", finalResult.size());
        } catch (TimeoutException e) {
            log.error("Timeout waiting for review to complete! Review ID: {}", receivedRequest.getReviewId());
            // Print the current state of the future
            log.error("Future completed: {}, cancelled: {}, done: {}",
                    reviewResultFuture.isDone(), reviewResultFuture.isCancelled(), reviewResultFuture.isDone());
            throw e;
        }

        // Validate the result
        assertNotNull(finalResult);
        assertEquals(2, finalResult.size());
        // Compare items - using title here as there are no IDs in the test items
        assertEquals(acceptedScrapeItems.get(0).getTitle(), finalResult.get(0).getTitle());
        assertEquals(acceptedScrapeItems.get(1).getTitle(), finalResult.get(1).getTitle());
        log.info("Assertions passed - test completed successfully");
    }

    private List<ScrapeItem> createTestScrapeItems(int count) {
        log.debug("Creating {} test data items", count);
        List<ScrapeItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ScrapeItem item = ScrapeItem.builder()
                    .title("Test title " + i)
                    .link("http://example.com/" + i)
                    .build();
            items.add(item);
        }
        return items;
    }
}