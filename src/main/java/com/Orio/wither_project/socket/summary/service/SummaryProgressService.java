package com.Orio.wither_project.socket.summary.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.Orio.wither_project.socket.summary.model.SummaryProgressDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SummaryProgressService {
    private static final Logger logger = LoggerFactory.getLogger(SummaryProgressService.class);
    private static final String PROGRESS_TOPIC = "/topic/progress/summary";

    private final SimpMessagingTemplate messagingTemplate;

    public void updateProgress(double progress) {
        SummaryProgressDTO progressUpdate = new SummaryProgressDTO(progress);
        logger.debug("Sending progress update: {}", progress);
        messagingTemplate.convertAndSend(PROGRESS_TOPIC, progressUpdate);
    }

    public void resetProgress() {
        SummaryProgressDTO resetUpdate = new SummaryProgressDTO(0.0);
        logger.debug("Resetting progress to {}", 0.0);
        messagingTemplate.convertAndSend(PROGRESS_TOPIC, resetUpdate);
    }
}
