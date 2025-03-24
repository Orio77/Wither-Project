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

import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.dto.DataModelReviewRequestDTO;
import com.Orio.wither_project.gather.model.dto.DataModelReviewResultDTO;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class DataModelReviewServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DataModelReviewService dataModelReviewService;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private final String WEBSOCKET_URI = "ws://localhost:{port}/ws";
    private final String REVIEW_TOPIC = "/topic/review/data-models";

    private CompletableFuture<DataModelReviewRequestDTO> receivedReviewRequest;

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
                return DataModelReviewRequestDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("Received message on topic: {}", REVIEW_TOPIC);
                receivedReviewRequest.complete((DataModelReviewRequestDTO) payload);
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

        // Create test data models
        List<DataModel> testModels = createTestDataModels(3);
        log.debug("Created {} test data models", testModels.size());

        // Start a separate thread to process the review
        log.info("Sending models for review asynchronously");
        CompletableFuture<List<DataModel>> reviewResultFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return dataModelReviewService.sendForReviewAndWait(testModels);
            } catch (Exception e) {
                log.error("Failed to send for review", e);
                throw new RuntimeException("Failed to send for review", e);
            }
        });

        // Wait for the review request to be received through WebSocket
        log.info("Waiting for review request to be received");
        DataModelReviewRequestDTO receivedRequest = receivedReviewRequest.get(10, TimeUnit.SECONDS);
        log.info("Received review request with ID: {}", receivedRequest.getReviewId());

        // Validate the received request
        assertNotNull(receivedRequest);
        assertEquals(3, receivedRequest.getModels().size());
        log.debug("Validated received request has {} models", receivedRequest.getModels().size());

        // Simulate frontend accepting only the first two models
        List<DataModel> acceptedModels = new ArrayList<>();
        acceptedModels.add(receivedRequest.getModels().get(0));
        acceptedModels.add(receivedRequest.getModels().get(1));
        log.info("Simulating frontend accepting {} models", acceptedModels.size());

        // Send back the review result using the same reviewId
        DataModelReviewResultDTO resultDTO = new DataModelReviewResultDTO();
        resultDTO.setReviewId(receivedRequest.getReviewId());
        resultDTO.setAcceptedModels(acceptedModels);

        log.info("Sending review completion to service");
        dataModelReviewService.completeReview(resultDTO);

        // Wait for the review process to complete
        log.info("Waiting for review future to complete");
        List<DataModel> finalResult = reviewResultFuture.get(10, TimeUnit.SECONDS);
        log.info("Review process completed with {} models", finalResult.size());

        // Validate the result
        assertNotNull(finalResult);
        assertEquals(2, finalResult.size());
        assertEquals(acceptedModels.get(0).getQuery(), finalResult.get(0).getQuery());
        assertEquals(acceptedModels.get(1).getQuery(), finalResult.get(1).getQuery());
        log.info("Assertions passed - test completed successfully");
    }

    private List<DataModel> createTestDataModels(int count) {
        log.debug("Creating {} test data models", count);
        List<DataModel> models = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DataModel model = DataModel.builder().query("Test Model " + i).build();
            models.add(model);
        }
        return models;
    }
}
