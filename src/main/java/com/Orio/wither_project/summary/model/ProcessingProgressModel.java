package com.Orio.wither_project.summary.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_progress")
@Data
@NoArgsConstructor
public class ProcessingProgressModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String fileName;

    private boolean conversionCompleted = false;
    private boolean metadataCompleted = false;
    private boolean contentsCompleted = false;

    private boolean pageSummariesCompleted = false;
    private int lastProcessedPageIndex = -1;

    private boolean chapterSummariesCompleted = false;
    private int lastProcessedChapterIndex = -1;

    private boolean documentSummaryCompleted = false;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        lastUpdated = LocalDateTime.now();
    }

    public ProcessingProgressModel(String fileName) {
        this.fileName = fileName;
    }
}