package com.Orio.wither_project.process.qa.service.format.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.model.Content;
import com.Orio.wither_project.gather.model.InformationType;
import com.Orio.wither_project.gather.model.TextBatch;
import com.Orio.wither_project.process.qa.config.QAProcessingConfig;
import com.Orio.wither_project.process.qa.service.format.ITextSplitService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicTextSplitService implements ITextSplitService {

    private final QAProcessingConfig config;

    @Override
    public List<TextBatch> splitContent(List<Content> items) {
        return items.stream()
                .map(this::splitContent)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public List<TextBatch> splitContent(Content item) {
        log.debug("Splitting content with source");

        String content = item.getContent();
        InformationType informationType = item.getInformationType();
        log.debug("Content: {}..., Information Type: {}",
                content.substring(0, Math.min(content.length(), 100)), informationType);

        int chunkSize = config.getContentPartMaxSize();
        int overlap = config.getContentOverlapCharacters();

        List<String> split = split(content, chunkSize, overlap);
        log.debug("Split content into {} chunks", split.size());
        int batchSize = config.getContentParts();

        List<TextBatch> textBatches = createBatches(split, batchSize);
        log.info("Created {} text batches", textBatches.size());

        log.debug("Created text batches: {}", textBatches.size());
        return textBatches;

    }

    private List<String> split(String content, int chunkSize, int overlap) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }

        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("Overlap must be non-negative and less than chunk size");
        }

        List<String> chunks = new ArrayList<>();

        // Handle case where content is shorter than chunk size
        if (content.length() <= chunkSize) {
            chunks.add(content);
            return chunks;
        }

        int position = 0;
        while (position < content.length()) {
            int endPosition = Math.min(position + chunkSize, content.length());

            // Try to find a natural breakpoint (period, newline, space) to end the chunk
            if (endPosition < content.length()) {
                int naturalBreak = findNaturalBreakpoint(content, endPosition - 20, endPosition);
                if (naturalBreak > position) {
                    endPosition = naturalBreak;
                }
            }

            chunks.add(content.substring(position, endPosition));

            // Move position for next chunk, considering overlap
            position = endPosition - overlap;

            // Handle edge case where we might get stuck with tiny chunks at the end
            if (position + chunkSize > content.length() && position < content.length() &&
                    content.length() - position < chunkSize / 2) {
                chunks.add(content.substring(position));
                break;
            }
        }

        return chunks;
    }

    private int findNaturalBreakpoint(String content, int startRange, int endRange) {
        startRange = Math.max(0, startRange);
        endRange = Math.min(content.length(), endRange);

        // First look for sentence endings (period followed by space or newline)
        for (int i = endRange; i > startRange; i--) {
            if (i < content.length() - 1 &&
                    content.charAt(i) == '.' &&
                    (content.charAt(i + 1) == ' ' || content.charAt(i + 1) == '\n')) {
                return i + 1; // Include the period in the current chunk
            }
        }

        // Then look for newlines
        for (int i = endRange; i > startRange; i--) {
            if (content.charAt(i) == '\n') {
                return i + 1; // Start after the newline
            }
        }

        // Finally fall back to spaces
        for (int i = endRange; i > startRange; i--) {
            if (content.charAt(i) == ' ') {
                return i + 1; // Start after the space
            }
        }

        // If no good breakpoint found, return the original end position
        return endRange;
    }

    private List<TextBatch> createBatches(List<String> split, int batchSize) {
        List<TextBatch> batches = new ArrayList<>();
        for (int i = 0; i < split.size(); i += batchSize) {
            int end = Math.min(i + batchSize, split.size());
            List<String> batchContent = split.subList(i, end);
            TextBatch textBatch = TextBatch.builder()
                    .content(batchContent)
                    .build();
            batches.add(textBatch);
        }
        return batches;
    }

}
