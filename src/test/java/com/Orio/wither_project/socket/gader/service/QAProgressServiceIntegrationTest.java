package com.Orio.wither_project.socket.gader.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
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

import com.Orio.wither_project.gather.model.QAModel;
import com.Orio.wither_project.socket.gader.model.QAProgressDTO;
import com.Orio.wither_project.socket.gader.service.impl.QAProgressService;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class QAProgressServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private QAProgressService qaProgressService;

    private WebSocketStompClient stompClient;
    private String websocketUrl;
    private final String TOPIC = "/topic/idea/qa";

    @BeforeEach
    void setup() {
        // Configure WebSocket client
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        websocketUrl = "ws://localhost:" + port + "/ws";
    }

    @Test
    void shouldSendQAUpdateMessageToWebSocketTopic() throws Exception {
        // Given
        QAModel qaModel = QAModel.builder().question("Test question").answer("Test answer").build();

        log.info("Test started with QAModel: {}", qaModel);

        // Create a future to capture the received message
        CompletableFuture<QAProgressDTO> completableFuture = new CompletableFuture<>();

        // Connect to the WebSocket using connectAsync
        StompSession stompSession = stompClient.connectAsync(websocketUrl,
                new StompSessionHandlerAdapter() {
                    // Add connection verification
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        log.info("Connection established with session id: {}", session.getSessionId());
                        super.afterConnected(session, connectedHeaders);
                    }

                    @Override
                    public void handleException(StompSession session, StompCommand command, StompHeaders headers,
                            byte[] payload, Throwable exception) {
                        log.error("Exception in STOMP session: ", exception);
                        super.handleException(session, command, headers, payload, exception);
                    }
                }).get(5, TimeUnit.SECONDS);

        // Subscribe to the topic
        stompSession.subscribe(TOPIC, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return QAProgressDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("Received message: {}", payload);
                completableFuture.complete((QAProgressDTO) payload);
            }
        });
        log.info("Subscribed to topic: {}", TOPIC);

        // Wait a moment to ensure subscription is established
        Thread.sleep(500);

        // When
        log.info("Sending QA update");
        qaProgressService.sendQAIdeasUpdate(qaModel);

        // Then
        log.info("Waiting for response");
        QAProgressDTO receivedDto = completableFuture.get(10, TimeUnit.SECONDS);
        assertNotNull(receivedDto, "Should receive a QA progress update");
        assertEquals("Test question", receivedDto.getQuestion(), "Question should match");
        assertEquals("Test answer", receivedDto.getAnswer(), "Answer should match");
        log.info("Test completed successfully");
    }
}