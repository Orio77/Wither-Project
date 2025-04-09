package com.Orio.wither_project.process.qa.service.filtration.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.model.TextBatch;
import com.Orio.wither_project.process.qa.service.filtration.ITextFiltrationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BasicTextFiltrationService implements ITextFiltrationService {

    // Regex pattern for identifying proper sentences
    private static final Pattern SENTENCE_PATTERN = Pattern.compile(
            "^[A-Z][^.!?]*[.!?]\\s*$", Pattern.MULTILINE);

    // Minimum characters required for a valid sentence
    private static final int MIN_SENTENCE_LENGTH = 10;

    // Minimum ratio of alphabetic characters to consider text as valid
    private static final double MIN_ALPHA_RATIO = 0.5;

    @Override
    public List<TextBatch> filter(List<TextBatch> textBatches) {
        log.info("Filtering {} text batches", textBatches.stream().flatMap(b -> b.getContent().stream()).count());

        List<TextBatch> filteredBatches = new ArrayList<>();

        for (TextBatch batch : textBatches) {
            TextBatch filteredBatch = filterBatch(batch);
            if (!filteredBatch.getContent().isEmpty()) {
                filteredBatches.add(filteredBatch);
            }
        }

        log.info("Filtered down to {} text batches",
                filteredBatches.stream().flatMap(fb -> fb.getContent().stream()).count());
        return filteredBatches;
    }

    /**
     * Filters a single TextBatch to remove non-sentence content
     * 
     * @param batch The batch to filter
     * @return A new TextBatch with only valid sentences
     */
    private TextBatch filterBatch(TextBatch batch) {
        List<String> validSentences = batch.getContent().stream()
                .filter(this::containsActualSentences)
                .collect(Collectors.toList());

        return TextBatch.builder()
                .content(validSentences)
                .source(batch.getSource())
                .build();
    }

    /**
     * Determines whether text contains actual sentences rather than scraped
     * nonsense
     * 
     * @param text The text to check
     * @return true if the text contains valid sentences, false otherwise
     */
    public boolean containsActualSentences(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // Check for minimum length
        if (text.length() < MIN_SENTENCE_LENGTH) {
            return false;
        }

        // Check for alphabetic character ratio
        long alphaCount = text.chars().filter(Character::isAlphabetic).count();
        double alphaRatio = (double) alphaCount / text.length();
        if (alphaRatio < MIN_ALPHA_RATIO) {
            return false;
        }

        // Check for sentence structure (starts with capital, ends with punctuation)
        if (!SENTENCE_PATTERN.matcher(text).find()) {
            // Not a strict sentence structure, but check if it's still meaningful text
            // Count words - at least 3 words to be considered valid text
            String[] words = text.trim().split("\\s+");
            if (words.length < 3) {
                return false;
            }
        }

        // Additional check for common HTML/code fragments
        if (text.contains("<") && text.contains(">")) {
            return false;
        }

        return true;
    }
}