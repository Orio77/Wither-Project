package com.Orio.wither_project.socket.gader.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.Orio.wither_project.gather.model.QAModel;
import com.Orio.wither_project.socket.gather.model.QAProgressDTO;
import com.Orio.wither_project.socket.gather.service.impl.QAProgressService;

@ExtendWith(MockitoExtension.class)
class QAProgressServiceTest {

    private QAProgressService qaProgressService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setUp() {
        qaProgressService = new QAProgressService(messagingTemplate);
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
