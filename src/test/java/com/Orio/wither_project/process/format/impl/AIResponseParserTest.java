package com.Orio.wither_project.process.format.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.model.Content;

import com.Orio.wither_project.util.AIResponseParser;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class AIResponseParserTest {

    private ChatResponse createMockResponse(String content) {
        ChatResponse mockResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage mockMessage = mock(AssistantMessage.class);
        Content mockContent = mock(Content.class);

        when(mockResponse.getResult()).thenReturn(mockGeneration);
        when(mockGeneration.getOutput()).thenReturn(mockMessage);
        when(mockMessage.getContent()).thenReturn(content);
        when(mockContent.getContent()).thenReturn(content);

        return mockResponse;
    }

    @Nested
    @DisplayName("Response Types Tests")
    class ResponseTypeTests {

        @Test
        @DisplayName("1. JSON response - should return JSON contents")
        void shouldReturnJsonContentWhenResponseContainsOnlyJson() {
            // Given
            String jsonContent = """
                    {"key": "value", "nested": {"data": true}}
                    """;
            ChatResponse mockResponse = createMockResponse(jsonContent);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            // Then
            assertEquals(jsonContent.trim(), result);
        }

        @Test
        @DisplayName("2. Text and JSON response - should return JSON contents")
        void shouldReturnJsonContentWhenResponseContainsTextFollowedByJson() {
            // Given
            String textAndJsonContent = """
                    Here is your requested data:
                    {"key": "value", "nested": {"data": true}}
                    """;
            ChatResponse mockResponse = createMockResponse(textAndJsonContent);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            // Then
            assertEquals("""
                    {"key": "value", "nested": {"data": true}}""", result);
        }

        @Test
        @DisplayName("3. JSON and text response - should return JSON contents")
        void shouldReturnJsonContentWhenResponseContainsJsonFollowedByText() {
            // Given
            String jsonAndTextContent = """
                    {"key": "value", "nested": {"data": true}}
                    Additional information can be found above.
                    """;
            ChatResponse mockResponse = createMockResponse(jsonAndTextContent);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            // Then
            assertEquals("""
                    {"key": "value", "nested": {"data": true}}""", result);
        }

        @Test
        @DisplayName("4. Text and JSON and text response - should return JSON contents")
        void shouldReturnJsonContentWhenResponseContainsTextJsonText() {
            // Given
            String mixedContent = """
                    Prefix text
                    {"key": "value", "nested": {"data": true}}
                    Suffix text
                    """;
            ChatResponse mockResponse = createMockResponse(mixedContent);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            // Then
            assertEquals("""
                    {"key": "value", "nested": {"data": true}}""", result);
        }

        @Test
        @DisplayName("5. Think tag - should ignore and return remaining content")
        void shouldIgnoreThinkTagsAndReturnRemainingContent() {
            // Given
            String contentWithThinkTags = """
                    <think>
                    This is my internal reasoning.
                    I should return JSON data.
                    </think>
                    Here is your answer:
                    {"result": "success", "value": 42}
                    """;
            ChatResponse mockResponse = createMockResponse(contentWithThinkTags);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            // Then
            assertEquals("""
                    {"result": "success", "value": 42}""", result);
        }

        @Test
        @DisplayName("6. Text only - should return the text content")
        void shouldReturnTextContentWhenResponseContainsOnlyText() {
            // Given
            String textContent = "This is a plain text response without any JSON.";
            ChatResponse mockResponse = createMockResponse(textContent);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            // Then
            assertEquals(textContent, result);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should return empty string for null input")
        void shouldReturnEmptyStringForNullInput() {
            assertEquals("", AIResponseParser.parseResponse(null));
        }

        @Test
        @DisplayName("Should return empty string for empty input")
        void shouldReturnEmptyStringForEmptyInput() {
            ChatResponse mockResponse = createMockResponse("");
            assertEquals("", AIResponseParser.parseResponse(mockResponse));
        }

        @Test
        @DisplayName("Should handle JSON with nested braces correctly")
        void shouldHandleJsonWithNestedBracesCorrectly() {
            // Given
            String nestedJsonContent = """
                    {"outer": {"inner": {"deepest": {"value": true}}}}
                    """;
            ChatResponse mockResponse = createMockResponse(nestedJsonContent);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            // Then
            assertEquals(nestedJsonContent.trim(), result);
        }

        @Test
        @DisplayName("Should handle multiple JSON objects by extracting the first one")
        void shouldHandleMultipleJsonObjectsByExtractingTheFirstOne() {
            // Given
            String multipleJsonContent = """
                    {"first": "object"}
                    {"second": "object"}
                    """;
            ChatResponse mockResponse = createMockResponse(multipleJsonContent);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            // Then
            assertEquals("""
                    {"first": "object"}""", result);
        }

        @Test
        @DisplayName("Should handle think tags with JSON inside them")
        void shouldHandleThinkTagsWithJsonInsideThem() {
            // Given
            String thinkTagWithJsonContent = """
                    <think>{"hidden": "data"}</think>
                    {"visible": "content"}
                    """;
            ChatResponse mockResponse = createMockResponse(thinkTagWithJsonContent);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            // Then
            assertEquals("""
                    {"visible": "content"}""", result);
        }

        @Test
        @DisplayName("Should handle incomplete JSON by returning empty object")
        void shouldHandleIncompleteJsonByReturningEmptyObject() {
            // Given
            String incompleteJsonContent = "This has an opening brace { but no closing one";
            ChatResponse mockResponse = createMockResponse(incompleteJsonContent);

            // When
            String result = AIResponseParser.parseResponse(mockResponse);

            log.info("Result: {}", result);

            // Then
            assertEquals(incompleteJsonContent, result);
        }
    }

    @Nested
    @DisplayName("Parse to Object Tests")
    class ParseToObjectTests {

        record TestPojo(String name, int age) {
        }

        record NestedPojo(String title, List<TestPojo> people) {
        }

        @Test
        @DisplayName("Should parse JSON to specified class")
        void shouldParseJsonToClass() {
            // Given
            String jsonContent = """
                    {"name": "John", "age": 30}
                    """;
            ChatResponse mockResponse = createMockResponse(jsonContent);

            // When
            TestPojo result = AIResponseParser.parseResponseToObject(mockResponse, TestPojo.class);

            // Then
            assertEquals("John", result.name());
            assertEquals(30, result.age());
        }

        @Test
        @DisplayName("Should parse nested JSON to complex class")
        void shouldParseNestedJsonToClass() {
            // Given
            String jsonContent = """
                    {"title": "Team", "people": [{"name": "John", "age": 30}, {"name": "Jane", "age": 25}]}
                    """;
            ChatResponse mockResponse = createMockResponse(jsonContent);

            // When
            NestedPojo result = AIResponseParser.parseResponseToObject(mockResponse, NestedPojo.class);

            // Then
            assertEquals("Team", result.title());
            assertEquals(2, result.people().size());
            assertEquals("John", result.people().get(0).name());
            assertEquals(25, result.people().get(1).age());
        }

        @Test
        @DisplayName("Should parse JSON with text prefix to class")
        void shouldParseJsonWithTextPrefixToClass() {
            // Given
            String jsonContent = """
                    Here is your data:
                    {"name": "John", "age": 30}
                    """;
            ChatResponse mockResponse = createMockResponse(jsonContent);

            // When
            TestPojo result = AIResponseParser.parseResponseToObject(mockResponse, TestPojo.class);

            // Then
            assertEquals("John", result.name());
            assertEquals(30, result.age());
        }

        @Test
        @DisplayName("Should parse JSON to TypeReference")
        void shouldParseJsonToTypeReference() {
            // Given
            String jsonContent = """
                    {"key1": "value1", "key2": "value2"}
                    """;
            ChatResponse mockResponse = createMockResponse(jsonContent);
            TypeReference<Map<String, String>> typeRef = new TypeReference<>() {
            };

            // When
            Map<String, String> result = AIResponseParser.parseResponseToObject(mockResponse, typeRef);

            // Then
            assertEquals("value1", result.get("key1"));
            assertEquals("value2", result.get("key2"));
        }

        @Test
        @DisplayName("Should parse JSON array to TypeReference")
        void shouldParseJsonArrayToTypeReference() {
            // Given
            String jsonContent = """
                    [{"name": "John", "age": 30}, {"name": "Jane", "age": 25}]
                    """;
            ChatResponse mockResponse = createMockResponse(jsonContent);
            TypeReference<List<TestPojo>> typeRef = new TypeReference<>() {
            };

            // When
            List<TestPojo> result = AIResponseParser.parseResponseToObject(mockResponse, typeRef);

            // Then
            assertEquals(2, result.size());
            assertEquals("John", result.get(0).name());
            assertEquals(30, result.get(0).age());
            assertEquals("Jane", result.get(1).name());
            assertEquals(25, result.get(1).age());
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertNull(AIResponseParser.parseResponseToObject(null, TestPojo.class));
            assertNull(AIResponseParser.parseResponseToObject(null, new TypeReference<TestPojo>() {
            }));
        }

        @Test
        @DisplayName("Should return null for empty input")
        void shouldReturnNullForEmptyInput() {
            ChatResponse mockResponse = createMockResponse("");
            assertNull(AIResponseParser.parseResponseToObject(mockResponse, TestPojo.class));
            assertNull(AIResponseParser.parseResponseToObject(mockResponse, new TypeReference<TestPojo>() {
            }));
        }

        @Test
        @DisplayName("Should return null for text-only content")
        void shouldReturnNullForTextOnlyContent() {
            // Given
            String textContent = "This is a plain text response without any JSON.";
            ChatResponse mockResponse = createMockResponse(textContent);

            // When/Then
            assertNull(AIResponseParser.parseResponseToObject(mockResponse, TestPojo.class));
            assertNull(AIResponseParser.parseResponseToObject(mockResponse, new TypeReference<TestPojo>() {
            }));
        }

        @Test
        @DisplayName("Should throw exception for invalid JSON format")
        void shouldThrowExceptionForInvalidJsonFormat() {
            // Given
            String invalidJsonContent = """
                    {"name": "John", "age": "thirty"}
                    """;
            ChatResponse mockResponse = createMockResponse(invalidJsonContent);

            // When/Then
            assertThrows(RuntimeException.class,
                    () -> AIResponseParser.parseResponseToObject(mockResponse, TestPojo.class));

            assertThrows(RuntimeException.class,
                    () -> AIResponseParser.parseResponseToObject(mockResponse, new TypeReference<TestPojo>() {
                    }));
        }

        @Test
        @DisplayName("Should ignore think tags and parse JSON")
        void shouldIgnoreThinkTagsAndParseJson() {
            // Given
            String contentWithThinkTags = """
                    <think>
                    This is my internal reasoning.
                    I should return JSON data.
                    </think>
                    {"name": "John", "age": 30}
                    """;
            ChatResponse mockResponse = createMockResponse(contentWithThinkTags);

            // When
            TestPojo result = AIResponseParser.parseResponseToObject(mockResponse, TestPojo.class);

            // Then
            assertEquals("John", result.name());
            assertEquals(30, result.age());
        }
    }
}