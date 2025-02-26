package com.Orio.wither_project.summary.service.progress;

import com.Orio.wither_project.summary.model.ProcessingProgressModel;
import com.Orio.wither_project.summary.repository.ProcessingProgressRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcessingProgressService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingProgressService.class);
    private final ProcessingProgressRepository progressRepository;

    public ProcessingProgressModel getOrCreateProgress(String fileName) {
        return progressRepository.findByFileName(fileName)
                .orElseGet(() -> {
                    logger.info("Creating new processing progress for file: {}", fileName);
                    return progressRepository.save(new ProcessingProgressModel(fileName));
                });
    }

    public void updateProgress(ProcessingProgressModel progress) {
        progressRepository.saveAndFlush(progress);
    }

    public Optional<ProcessingProgressModel> getProgress(String fileName) {
        return progressRepository.findByFileName(fileName);
    }

    public void resetProgress(String fileName) {
        progressRepository.findByFileName(fileName).ifPresent(progress -> {
            progress.setConversionCompleted(false);
            progress.setMetadataCompleted(false);
            progress.setContentsCompleted(false);
            progress.setPageSummariesCompleted(false);
            progress.setLastProcessedPageIndex(-1);
            progress.setChapterSummariesCompleted(false);
            progress.setLastProcessedChapterIndex(-1);
            progress.setDocumentSummaryCompleted(false);
            progressRepository.save(progress);
        });
    }
}