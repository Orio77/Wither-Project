package com.Orio.wither_project.socket.gather.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.Orio.wither_project.process.qa.model.QAModel;
import com.Orio.wither_project.socket.process.model.QAProgressDTO;
import com.Orio.wither_project.socket.process.service.impl.WebSocketQAProgressNotifier;

@ExtendWith(MockitoExtension.class)
class QAProgressServiceTest {

    private WebSocketQAProgressNotifier qaProgressService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setUp() {
        qaProgressService = new WebSocketQAProgressNotifier(messagingTemplate);
    }

    @Test
    void sendQAUpdate_shouldSendMessageToTopic() {
        // Given
        QAModel qaModel = QAModel.builder().answer("Test answer").question("Test question").build();
        // Configure qaModel as needed for testing

        // When
        qaProgressService.sendQAIdeasUpdate(qaModel);

        // Then
        verify(messagingTemplate).convertAndSend(
                eq("/topic/idea/qa"),
                any(QAProgressDTO.class));
    }

    @Test
    void sendQAUpdate_shouldCreateCorrectDTO() {
        // Given
        QAModel qaModel = QAModel.builder().answer("Test answer").question("Test question").build();

        // When
        qaProgressService.sendQAIdeasUpdate(qaModel);

        // Then
        verify(messagingTemplate).convertAndSend(
                eq("/topic/idea/qa"),
                eq(QAProgressDTO.builder()
                        .question("Test question")
                        .answer("Test answer")
                        .build()));
    }
}
