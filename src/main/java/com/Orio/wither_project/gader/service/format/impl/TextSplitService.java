package com.Orio.wither_project.gader.service.format.impl;

import com.Orio.wither_project.gader.config.ProcessingConfig;
import com.Orio.wither_project.gader.model.ContentWithSource;
import com.Orio.wither_project.gader.model.ScrapedTextBatch;
import com.Orio.wither_project.gader.model.ScrapeResult.ScrapeItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextSplitService {

    private final ProcessingConfig config;

    public List<ContentWithSource> getContentWithSources(List<ScrapeItem> items) {
        log.debug("Processing {} scrape items to extract content with sources", items.size());
        List<ContentWithSource> result = items.stream()
                .filter(item -> item != null && item.getContent() != null && item.getLink() != null)
                .map(item -> {
                    ContentWithSource contentWithSource = new ContentWithSource(item.getContent(), item.getLink());
                    log.debug("Extracted content with source: {}", contentWithSource);
                    return contentWithSource;
                })
                .collect(Collectors.toList());
        log.debug("Extracted {} valid content items with sources", result.size());
        return result;
    }

    public List<ScrapedTextBatch> splitContent(List<ContentWithSource> contentWithSources) {
        int contentPartsPerPart = config.getContentParts();
        int maxCharactersPerContent = config.getContentPartMaxSize();
        int overlapCharacters = config.getContentOverlapCharacters();
        int contentSize = contentWithSources.size();
        List<ScrapedTextBatch> parts = new ArrayList<>();

        log.debug("Splitting {} content items into parts of {} each (max {} chars per content, {} char overlap)",
                contentSize, contentPartsPerPart, maxCharactersPerContent, overlapCharacters);

        for (int i = 0; i < contentSize; i += contentPartsPerPart) {
            log.debug("Processing batch starting at index {}", i);
            int end = Math.min(i + contentPartsPerPart, contentSize);
            List<String> contentSubList = new ArrayList<>();
            String source = null;

            for (int j = i; j < end; j++) {
                ContentWithSource cws = contentWithSources.get(j);
                String content = cws.getContent();
                log.debug("Processing item {}/{} with source: {}", j, end - 1, cws.getSource());

                if (source == null) {
                    source = cws.getSource();
                    log.debug("Using source: {}", source);
                }

                if (content != null && content.length() > maxCharactersPerContent) {
                    log.debug("Content length {} exceeds max size {}, splitting with overlap", content.length(),
                            maxCharactersPerContent);
                    List<String> splitStrings = splitStringWithOverlap(content, maxCharactersPerContent,
                            overlapCharacters);
                    contentSubList.addAll(splitStrings);
                    log.debug("Split content of {} chars into {} chunks", content.length(), splitStrings.size());
                } else if (content != null) {
                    log.debug("Adding content of length {} directly (under max size)", content.length());
                    contentSubList.add(content);
                } else {
                    log.debug("Skipping null content");
                }
            }

            ScrapedTextBatch part = ScrapedTextBatch.builder()
                    .content(contentSubList)
                    .source(source)
                    .build();

            parts.add(part);
            log.debug("Created part {}, with {} content items from batch {}-{}", parts.size(), contentSubList.size(), i,
                    end - 1);
        }

        log.info("Created {} parts from {} content items", parts.size(), contentSize);
        return parts;
    }

    public List<String> splitStringWithOverlap(String content, int maxChars, int overlap) {
        List<String> result = new ArrayList<>();
        int length = content.length();
        int startPos = 0;
        log.debug("Splitting string of length {} with max chars {} and overlap {}", length, maxChars, overlap);

        // If content is already shorter than maxChars, return it as a single item
        if (length <= maxChars) {
            result.add(content.trim());
            log.debug("Content length {} is less than max chars {}, returning as single item", length, maxChars);
            return result;
        }

        while (startPos < length) {
            int endPos = Math.min(startPos + maxChars, length);
            log.debug("Processing substring from {} to {} of {}", startPos, endPos, length);

            if (endPos < length) {
                int periodPos = content.lastIndexOf(".", endPos);
                int questionPos = content.lastIndexOf("?", endPos);
                int exclamationPos = content.lastIndexOf("!", endPos);
                int sentenceBreak = Math.max(Math.max(periodPos, questionPos), exclamationPos);

                if (sentenceBreak > startPos && sentenceBreak > endPos - (maxChars * 0.2)) {
                    endPos = sentenceBreak + 1;
                } else {
                    int spacePos = content.lastIndexOf(" ", endPos);
                    if (spacePos > startPos && spacePos > endPos - (maxChars * 0.2)) {
                        endPos = spacePos + 1;
                    }
                }
            }

            String substring = content.substring(startPos, endPos).trim();
            result.add(substring);
            log.debug("Added substring of length {}", substring.length());

            // Only increment startPos if we need to continue splitting
            if (endPos >= length) {
                break;
            }

            startPos = endPos - overlap;
            log.debug("New startPos: {}", startPos);
        }

        log.debug("Split into {} parts", result.size());
        return result;
    }
}
