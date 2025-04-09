package com.Orio.wither_project.gather.service.format.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.Orio.wither_project.gather.exception.AnswerNotFoundException;
import com.Orio.wither_project.process.qa.service.format.impl.ThreeWordTextSearchService;

class TextSearchServiceTest {

    private ThreeWordTextSearchService service;

    @BeforeEach
    void setUp() {
        service = new ThreeWordTextSearchService();
    }

    @Nested
    @DisplayName("findAnswer Tests")
    class FindAnswerTests {

        @Test
        @DisplayName("Should find answer successfully with exact match")
        void shouldFindAnswerSuccessfully() {
            // Arrange
            String text = "This is a sample text with an important answer here. The answer continues until this point.";
            String firstWords = "an important answer";
            String lastWords = "until this point";

            // Act
            String result = service.findAnswer(firstWords, lastWords, text);

            // Assert
            assertEquals("an important answer here. The answer continues until this point", result);
        }

        @Test
        @DisplayName("Should normalize text before searching")
        void shouldNormalizeTextBeforeSearching() {
            // Arrange
            String text = "This is a sample, with SOME punctuation. Answer starts HERE. It ends there!";
            String firstWords = "Answer starts here";
            String lastWords = "It ends there";

            // Act
            String result = service.findAnswer(firstWords, lastWords, text);

            // Assert
            assertEquals("Answer starts HERE. It ends there", result);
        }

        @Test
        @DisplayName("Should throw exception when start phrase not found")
        void shouldThrowExceptionWhenStartPhraseNotFound() {
            // Arrange
            String text = "This is a sample text.";
            String firstWords = "nonexistent phrase";
            String lastWords = "sample";

            // Act & Assert
            assertThrows(AnswerNotFoundException.class,
                    () -> service.findAnswer(firstWords, lastWords, text));
        }

        @Test
        @DisplayName("Should throw exception when end phrase not found")
        void shouldThrowExceptionWhenEndPhraseNotFound() {
            // Arrange
            String text = "This is a sample text.";
            String firstWords = "This is";
            String lastWords = "nonexistent phrase";

            // Act & Assert
            assertThrows(AnswerNotFoundException.class,
                    () -> service.findAnswer(firstWords, lastWords, text));
        }

        @Test
        @DisplayName("Should throw exception when end phrase is before start phrase")
        void shouldThrowExceptionWhenEndPhraseIsBeforeStartPhrase() {
            // Arrange
            String text = "First part. Second part.";
            String firstWords = "Second part";
            String lastWords = "First part";

            // Act & Assert
            assertThrows(AnswerNotFoundException.class,
                    () -> service.findAnswer(firstWords, lastWords, text));
        }

        @Test
        @DisplayName("Should throw exception for null parameters")
        void shouldThrowExceptionForNullParameters() {
            // Arrange
            String text = "Sample text";

            // Act & Assert
            assertThrows(AnswerNotFoundException.class,
                    () -> service.findAnswer(null, "end", text));
            assertThrows(AnswerNotFoundException.class,
                    () -> service.findAnswer("start", null, text));
            assertThrows(AnswerNotFoundException.class,
                    () -> service.findAnswer("start", "end", null));
        }
    }

    @Nested
    @DisplayName("normalizeSearchText Tests")
    class NormalizeSearchTextTests {

        @Test
        @DisplayName("Should convert text to lowercase")
        void shouldConvertToLowercase() {
            assertEquals("test string", service.normalizeSearchText("TEST STRING"));
        }

        @Test
        @DisplayName("Should remove punctuation")
        void shouldRemovePunctuation() {
            assertEquals("hello world", service.normalizeSearchText("hello, world!"));
        }

        @Test
        @DisplayName("Should normalize whitespace")
        void shouldNormalizeWhitespace() {
            assertEquals("multiple spaces here", service.normalizeSearchText("multiple   spaces    here"));
        }

        @Test
        @DisplayName("Should trim whitespace")
        void shouldTrimWhitespace() {
            assertEquals("trimmed text", service.normalizeSearchText("  trimmed text  "));
        }

        @Test
        @DisplayName("Should handle null input")
        void shouldHandleNullInput() {
            assertEquals("", service.normalizeSearchText(null));
        }
    }

    @Nested
    @DisplayName("findBestMatch Tests")
    class FindBestMatchTests {

        @Test
        @DisplayName("Should find exact match")
        void shouldFindExactMatch() {
            assertEquals(5, service.findBestMatch("Hello world", "world"));
        }

        @Test
        @DisplayName("Should return -1 when no match found")
        void shouldReturnNegativeOneWhenNoMatchFound() {
            assertEquals(-1, service.findBestMatch("Hello world", "universe"));
        }

        @Test
        @DisplayName("Should find match after offset")
        void shouldFindMatchAfterOffset() {
            assertEquals(12, service.findBestMatch("Hello world, world again", "world", 10));
        }

        @Test
        @DisplayName("Should return -1 when no match found after offset")
        void shouldReturnNegativeOneWhenNoMatchFoundAfterOffset() {
            assertEquals(-1, service.findBestMatch("Hello world", "world", 6));
        }
    }
}
