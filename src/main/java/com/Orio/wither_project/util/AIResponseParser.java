package com.Orio.wither_project.util;

import java.util.regex.Pattern;

import org.springframework.ai.chat.model.ChatResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AIResponseParser {

    private static final Pattern THINK_TAG_PATTERN = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sealed hierarchy to represent different types of AI responses
     */
    private sealed interface AIResponse permits TextResponse, JsonResponse {
        String getContent();
    }

    private record TextResponse(String content) implements AIResponse {
        @Override
        public String getContent() {
            return content;
        }
    }

    private record JsonResponse(String content) implements AIResponse {
        @Override
        public String getContent() {
            return content;
        }

        public <T> T toObject(Class<T> clazz) {
            try {
                return objectMapper.readValue(content, clazz);
            } catch (Exception e) {
                log.error("Failed to convert JSON to {}: {}", clazz.getName(), e.getMessage());
                throw new RuntimeException("JSON conversion failed", e);
            }
        }

        public <T> T toObject(TypeReference<T> typeReference) {
            try {
                return objectMapper.readValue(content, typeReference);
            } catch (Exception e) {
                log.error("Failed to convert JSON: {}", e.getMessage());
                throw new RuntimeException("JSON conversion failed", e);
            }
        }
    }

    /**
     * Parses an AI response according to rules:
     * 1. json response - just return json contents
     * 2. text and json response - return json contents
     * 3. json and text - return json contents
     * 4. text and json and text - return json contents
     * 5. think tag - ignore it
     * 6. text - simply return the text
     *
     * @param response The AI response to parse
     * @return Parsed content according to rules
     */
    public static String parseResponse(ChatResponse response) {
        String aiResponse = getContent(response);
        return switch (aiResponse) {
            case null, "" -> "";
            case String s -> {
                // Remove think tags
                String cleanedResponse = THINK_TAG_PATTERN.matcher(s).replaceAll("").trim();

                // Extract the appropriate content based on response type
                yield switch (analyzeResponse(cleanedResponse)) {
                    case JsonResponse(String json) -> json;
                    case TextResponse(String text) -> text;
                };
            }
        };
    }

    private static String getContent(ChatResponse response) {
        switch (response) {
            case null -> {
                return "";
            }
            case ChatResponse r -> {
                return r.getResult().getOutput().getContent();
            }
        }

    }

    /**
     * Analyzes the response content and categorizes it
     */
    private static AIResponse analyzeResponse(String input) {
        return containsJson(input)
                ? new JsonResponse(extractJson(input))
                : new TextResponse(input);
    }

    /**
     * Checks if the input contains a JSON object
     */
    private static boolean containsJson(String input) {
        return (input.indexOf('{') != -1 && input.indexOf('}') != -1) ||
                (input.indexOf('[') != -1 && input.indexOf(']') != -1);
    }

    /**
     * Extracts the first valid JSON object from a string
     */
    private static String extractJson(String input) {
        int openBraceIndex = input.indexOf('{');
        int openBracketIndex = input.indexOf('[');

        // Determine if we're extracting an object or array
        boolean isArray = (openBracketIndex != -1 && (openBraceIndex == -1 || openBracketIndex < openBraceIndex));
        int startIndex = isArray ? openBracketIndex : openBraceIndex;

        if (startIndex == -1) {
            return isArray ? "[]" : "{}";
        }

        int openCount = 0;
        int endIndex = -1;
        char openChar = isArray ? '[' : '{';
        char closeChar = isArray ? ']' : '}';

        for (int i = startIndex; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == openChar) {
                openCount++;
            } else if (c == closeChar) {
                openCount--;
                if (openCount == 0) {
                    endIndex = i + 1;
                    break;
                }
            }
        }

        return endIndex != -1 ? input.substring(startIndex, endIndex) : (isArray ? "[]" : "{}");
    }

    public static <T> T parseResponseToObject(ChatResponse response, Class<T> clazz) {
        String aiResponse = getContent(response);
        if (aiResponse == null || aiResponse.isEmpty()) {
            return null;
        }

        String cleanedResponse = THINK_TAG_PATTERN.matcher(aiResponse).replaceAll("").trim();
        AIResponse analyzedResponse = analyzeResponse(cleanedResponse);

        if (analyzedResponse instanceof JsonResponse jsonResponse) {
            return jsonResponse.toObject(clazz);
        }

        return null;
    }

    public static <T> T parseResponseToObject(ChatResponse response, TypeReference<T> typeReference) {
        String aiResponse = getContent(response);
        if (aiResponse == null || aiResponse.isEmpty()) {
            return null;
        }

        String cleanedResponse = THINK_TAG_PATTERN.matcher(aiResponse).replaceAll("").trim();
        AIResponse analyzedResponse = analyzeResponse(cleanedResponse);

        if (analyzedResponse instanceof JsonResponse jsonResponse) {
            return jsonResponse.toObject(typeReference);
        }

        return null;
    }
}