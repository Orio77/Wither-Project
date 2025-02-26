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
    private static final String PROGRESS_DESTINATION = "/topic/progress";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Updates and broadcasts the progress of a PDF processing operation.
     * 
     * @param progress Progress value between 0 and 1
     */
    public void updateProgress(double progress) {
        try {
            // Normalize progress to ensure it's between 0 and 1
            double normalizedProgress = Math.max(0.0, Math.min(1.0, progress));

            // Create progress DTO with normalized value
            SummaryProgressDTO progressDTO = new SummaryProgressDTO();
            progressDTO.setProgress(normalizedProgress);

            // Send the progress update to clients
            messagingTemplate.convertAndSend(PROGRESS_DESTINATION, progressDTO);

            logger.debug("Progress update sent: {}%", Math.round(normalizedProgress * 100));
        } catch (Exception e) {
            logger.error("Failed to send progress update: {}", e.getMessage(), e);
        }
    }

    public void resetProgress() {
        SummaryProgressDTO resetUpdate = new SummaryProgressDTO(0.0);
        logger.debug("Resetting progress to {}", 0.0);
        messagingTemplate.convertAndSend(PROGRESS_DESTINATION, resetUpdate);
    }
}
