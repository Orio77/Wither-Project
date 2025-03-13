package com.Orio.wither_project.socket.gader.service.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.gader.model.QAModel;
import com.Orio.wither_project.socket.gader.model.QAProgressDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class QAProgressService {
    private static final String QA_PROGRESS_TOPIC = "/topic/progress/qa";
    private static final String QA_IDEAS_TOPIC = "/topic/idea/qa";

    private final SimpMessagingTemplate messagingTemplate;

    public void sendQAIdeasUpdate(QAModel qaModel) {
        QAProgressDTO updateDTO = QAProgressDTO.builder()
                .question(qaModel.getQuestion())
                .answer(qaModel.getAnswer())
                .build();
        log.debug("Sending QA ideas update: {}", updateDTO);
        messagingTemplate.convertAndSend(QA_IDEAS_TOPIC, updateDTO);
    }

    public void sendQAProgressUpdate(int processed, int total) {
        double progress = (double) processed / total;
        log.debug("Sending QA progress update: {}/{}", processed, total);

        messagingTemplate.convertAndSend(QA_PROGRESS_TOPIC, progress);
    }
}
