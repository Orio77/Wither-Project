package com.Orio.wither_project.gather.service.format.impl;

import org.springframework.stereotype.Service;

import com.Orio.wither_project.gather.exception.AnswerNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TextSearchService {

    public String findAnswer(String firstThreeWordsOfAnswer, String lastThreeWordsOfAnswer, String text) {
        if (firstThreeWordsOfAnswer == null || lastThreeWordsOfAnswer == null || text == null) {
            throw new AnswerNotFoundException("Cannot find answer with null parameters");
        }

        // Use a simple normalization that only trims and converts to lower-case.
        // This preserves the original string length for consistent indexing.
        String lowerText = text.toLowerCase();
        String lowerFirst = firstThreeWordsOfAnswer.trim().toLowerCase();
        String lowerLast = lastThreeWordsOfAnswer.trim().toLowerCase();

        log.debug("Searching for answer between '{}' and '{}' in text: {}", lowerFirst, lowerLast, lowerText);

        int startIndex = lowerText.indexOf(lowerFirst);
        if (startIndex == -1) {
            throw new AnswerNotFoundException("Could not find starting phrase: '" + lowerFirst + "'");
        }

        int endIndex = lowerText.indexOf(lowerLast, startIndex);
        if (endIndex == -1) {
            throw new AnswerNotFoundException(
                    "Could not find ending phrase: '" + lowerLast + "' after position " + startIndex);
        }

        int distance = endIndex - startIndex;
        if (distance < 0) {
            throw new AnswerNotFoundException(
                    "Invalid answer range: start=" + startIndex + ", end=" + endIndex + ", distance=" + distance);
        }

        if (distance > 10000) {
            log.warn("Unusually large answer detected: {} characters", distance);
        }

        int answerEndPosition = endIndex + lowerLast.length();
        if (answerEndPosition <= text.length()) {
            String answer = text.substring(startIndex, answerEndPosition);
            log.debug("Found answer with length {} at positions {}:{}", answer.trim().length(), startIndex,
                    answerEndPosition);
            log.debug("Answer: {}", answer);
            return answer.trim();
        } else {
            throw new AnswerNotFoundException(
                    "Answer end position (" + answerEndPosition + ") exceeds content length (" + text.length() + ")");
        }
    }

    // This method is no longer used by findAnswer but is kept for potential other
    // uses.
    public String normalizeSearchText(String text) {
        // Simpler normalization: trim and lower-case without changing
        // punctuation/spacing.
        return text == null ? "" : text.trim().toLowerCase();
    }

    public int findBestMatch(String content, String searchText) {
        return findBestMatch(content, searchText, 0);
    }

    public int findBestMatch(String content, String searchText, int offset) {
        return content.indexOf(searchText, offset);
    }
}
