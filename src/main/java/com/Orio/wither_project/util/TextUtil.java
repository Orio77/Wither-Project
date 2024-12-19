package com.Orio.wither_project.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextUtil {

    private static final Logger logger = LoggerFactory.getLogger(TextUtil.class);

    // A method that receives a text, first and last three words of a fragment
    // within, finds it and returns it
    public static String parse(String text, String first_words, String last_words) {
        if (text == null || first_words == null || last_words == null || text.isEmpty() || first_words.isEmpty()
                || last_words.isEmpty()) {
            logger.error("Received argument was null or empty, text: {}, first_words: {}, last_words: {}", text,
                    first_words, last_words);
            throw new IllegalArgumentException("Arguments cannot be null nor empty");
        }

        // Convert text, first_words, and last_words to lowercase for case-insensitive
        // search
        String lowerText = text.toLowerCase();
        String lowerFirstWords = first_words.toLowerCase();
        String lowerLastWords = last_words.toLowerCase();

        // Find the starting index of the first words
        int startIndex = lowerText.indexOf(lowerFirstWords);
        if (startIndex == -1) {
            return null; // First words not found
        }

        // Find the starting index of the last words after the first words
        int endIndex = lowerText.indexOf(lowerLastWords, startIndex + lowerFirstWords.length());
        if (endIndex == -1) {
            return null; // Last words not found
        }

        // Return the substring from the original text that matches the fragment
        String res = text.substring(startIndex, endIndex + last_words.length());
        logger.info("Found text: {}", res);
        return res;
    }

}
