package com.Orio.wither_project.gather.service.format.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.Orio.wither_project.gather.model.QAModel;
import com.Orio.wither_project.gather.service.format.impl.QAModelExtractionService;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Tests for {@link QAModelExtractionService} which verify the extraction of QA
 * models
 * from AI-generated responses.
 */
@SpringBootTest
@Import(TestConfig.class)
@DisplayName("QA Model Extraction Service")
public class QAModelExtractionServiceTest {

  private static final String SAMPLE_TEXT = "The quick brown fox jumps over the lazy dog. " +
      "Artificial intelligence is transforming how we interact with technology. " +
      "Java is a popular programming language known for its platform independence. " +
      "Spring Boot makes it easy to create stand-alone, production-grade Spring based applications.";

  private static final String VALID_QA_PAIRS_FORMAT = """
      {
        "qaPairs": [
          {
            "question": "%s",
            "first_three_words_of_an_answer": "%s",
            "last_three_words_of_an_answer": "%s"
          }
        ]
      }
      """;

  @Autowired
  private QAModelExtractionService qaModelExtractionService;

  private String testText;

  @BeforeEach
  public void setUp() {
    testText = SAMPLE_TEXT;
  }

  @Nested
  @DisplayName("Successful extractions")
  class SuccessfulExtractions {

    @Test
    @DisplayName("Should extract multiple QA models from valid response")
    public void shouldExtractMultipleQAModels() throws Exception {
      // Arrange
      String responseJson = """
          {
            "qaPairs": [
              {
                "question": "What animal jumps over the dog?",
                "first_three_words_of_an_answer": "The quick brown",
                "last_three_words_of_an_answer": "the lazy dog."
              },
              {
                "question": "What is transforming how we interact with technology?",
                "first_three_words_of_an_answer": "Artificial intelligence is",
                "last_three_words_of_an_answer": "with technology"
              }
            ]
          }
          """;
      ChatResponse chatResponse = createChatResponse(responseJson);

      // Act
      List<QAModel> results = qaModelExtractionService.extractQAModels(chatResponse, testText);

      // Assert
      assertThat(results)
          .hasSize(2)
          .extracting(QAModel::getQuestion)
          .containsExactly(
              "What animal jumps over the dog?",
              "What is transforming how we interact with technology?");

      assertThat(results)
          .extracting(QAModel::getAnswer)
          .containsExactly(
              "The quick brown fox jumps over the lazy dog.",
              "Artificial intelligence is transforming how we interact with technology");
    }

    @ParameterizedTest(name = "Using text: {0}")
    @ValueSource(strings = {
        "The Earth is the third planet from the Sun and the only astronomical object known to harbor life.",
        "Python is a high-level, general-purpose programming language. Its design philosophy emphasizes code readability."
    })
    @DisplayName("Should extract QA models from various text samples")
    public void shouldExtractFromVariousTextSamples(String testText) throws Exception {
      // Arrange
      String firstThreeWords = extractFirstThreeWords(testText);
      String lastThreeWords = extractLastThreeWords(testText);

      String responseJson = String.format(VALID_QA_PAIRS_FORMAT,
          "Sample question about the text",
          firstThreeWords,
          lastThreeWords);

      ChatResponse chatResponse = createChatResponse(responseJson);

      // Act
      List<QAModel> results = qaModelExtractionService.extractQAModels(chatResponse, testText);

      // Assert
      assertThat(results)
          .isNotEmpty()
          .hasSize(1);

      assertThat(results.get(0).getAnswer())
          .as("The extracted answer should match the source text")
          .isEqualTo(testText);

      assertThat(results.get(0).getQuestion())
          .isEqualTo("Sample question about the text");
    }
  }

  @Nested
  @DisplayName("Error handling")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should return empty list for empty QA pairs")
    public void shouldReturnEmptyListForEmptyQAPairs() throws Exception {
      // Arrange
      String responseJson = """
          {
            "qaPairs": []
          }
          """;
      ChatResponse chatResponse = createChatResponse(responseJson);

      // Act
      List<QAModel> results = qaModelExtractionService.extractQAModels(chatResponse, testText);

      // Assert
      assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should handle missing fields in QA pairs")
    public void shouldHandleMissingFieldsInQAPairs() throws Exception {
      // Arrange
      String responseJson = """
          {
            "qaPairs": [
              {
                "question": "What is Java known for?",
                "first_three_words_of_an_answer": "Java is a",
                "last_three_words_of_an_answer": ""
              },
              {
                "question": "",
                "first_three_words_of_an_answer": "Spring Boot makes",
                "last_three_words_of_an_answer": "Spring based applications."
              }
            ]
          }
          """;
      ChatResponse chatResponse = createChatResponse(responseJson);

      // Act
      List<QAModel> results = qaModelExtractionService.extractQAModels(chatResponse, testText);

      // Assert
      assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should handle response without QA pairs field")
    public void shouldHandleResponseWithoutQAPairsField() throws Exception {
      // Arrange
      String responseJson = """
          {
            "someOtherField": "value"
          }
          """;
      ChatResponse chatResponse = createChatResponse(responseJson);

      // Act
      List<QAModel> results = qaModelExtractionService.extractQAModels(chatResponse, testText);

      // Assert
      assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should handle answers not found in text")
    public void shouldHandleAnswersNotFoundInText() throws Exception {
      // Arrange
      String responseJson = """
          {
            "qaPairs": [
              {
                "question": "What is not in the text?",
                "first_three_words_of_an_answer": "This doesn't exist",
                "last_three_words_of_an_answer": "in the text"
              }
            ]
          }
          """;
      ChatResponse chatResponse = createChatResponse(responseJson);

      // Act
      List<QAModel> results = qaModelExtractionService.extractQAModels(chatResponse, testText);

      // Assert
      assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception for invalid JSON response")
    public void shouldThrowExceptionForInvalidJsonResponse() {
      // Arrange
      String invalidJson = "This is not valid JSON";
      ChatResponse chatResponse = createChatResponse(invalidJson);

      // Act & Assert
      assertThrows(JsonProcessingException.class, () -> {
        qaModelExtractionService.extractQAModels(chatResponse, testText);
      }, "Should throw exception when response contains invalid JSON");
    }

    @Test
    @DisplayName("Should throw exception when passing null parameters")
    public void shouldThrowExceptionForNullParameters() {
      // Arrange
      ChatResponse chatResponse = createChatResponse("{}");

      // Act & Assert
      assertThrows(NullPointerException.class, () -> {
        qaModelExtractionService.extractQAModels(null, testText);
      }, "Should throw NullPointerException when response is null");

      assertThrows(NullPointerException.class, () -> {
        qaModelExtractionService.extractQAModels(chatResponse, null);
      }, "Should throw NullPointerException when text is null");
    }
  }

  @Nested
  @DisplayName("Edge cases")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle malformed but valid JSON")
    public void shouldHandleMalformedButValidJson() throws Exception {
      // Arrange
      String responseJson = "{\"qaPairs\":[{\"question\":\"Test\",\"first_three_words_of_an_answer\":\"The quick brown\",\"last_three_words_of_an_answer\":\"the lazy dog.\"}]}";
      ChatResponse chatResponse = createChatResponse(responseJson);

      // Act
      List<QAModel> results = qaModelExtractionService.extractQAModels(chatResponse, testText);

      // Assert
      assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("Should handle response with extra fields")
    public void shouldHandleResponseWithExtraFields() throws Exception {
      // Arrange
      String responseJson = """
          {
            "qaPairs": [
              {
                "question": "What animal jumps over the dog?",
                "first_three_words_of_an_answer": "The quick brown",
                "last_three_words_of_an_answer": "the lazy dog.",
                "extraField": "This should be ignored",
                "confidence": 0.95
              }
            ],
            "metadata": {
              "source": "test"
            }
          }
          """;
      ChatResponse chatResponse = createChatResponse(responseJson);

      // Act
      List<QAModel> results = qaModelExtractionService.extractQAModels(chatResponse, testText);

      // Assert
      assertThat(results).hasSize(1);
      assertThat(results.get(0).getQuestion()).isEqualTo("What animal jumps over the dog?");
    }
  }

  /**
   * Creates a ChatResponse with the given content.
   * 
   * @param content The JSON content for the response
   * @return A ChatResponse object containing the content
   */
  private ChatResponse createChatResponse(String content) {
    return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
  }

  /**
   * Extracts the first three words from a text.
   * 
   * @param text The source text
   * @return The first three words or less if the text is shorter
   */
  private String extractFirstThreeWords(String text) {
    String[] words = text.split("\\s+");
    int wordCount = Math.min(3, words.length);
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < wordCount; i++) {
      if (i > 0)
        result.append(" ");
      result.append(words[i]);
    }

    return result.toString();
  }

  /**
   * Extracts the last three words from a text.
   * 
   * @param text The source text
   * @return The last three words or less if the text is shorter
   */
  private String extractLastThreeWords(String text) {
    String[] words = text.split("\\s+");
    int wordCount = Math.min(3, words.length);
    StringBuilder result = new StringBuilder();

    for (int i = words.length - wordCount; i < words.length; i++) {
      if (i > words.length - wordCount)
        result.append(" ");
      result.append(words[i]);
    }

    return result.toString();
  }
}
