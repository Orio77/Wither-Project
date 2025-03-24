package com.Orio.wither_project.gather.service.format.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.gather.model.DataModel;
import com.Orio.wither_project.gather.model.InformationPiece;
import com.Orio.wither_project.gather.model.QAModel;
import com.Orio.wither_project.gather.model.ScrapeResult.ScrapeItem;
import com.Orio.wither_project.gather.service.format.IFormatService;

@SpringBootTest
@ActiveProfiles("test")
public class BasicFormatServiceTest {

        @Autowired
        private IFormatService formatService;

        @Test
        public void testFormatQAModels() throws Exception {
                // Setup test data - JSON response with QA pairs
                String jsonContent = "{"
                                + "\"qaPairs\": ["
                                + "  {"
                                + "    \"question\": \"What is Spring Boot?\","
                                + "    \"first_three_words_of_an_answer\": \"Spring Boot is\","
                                + "    \"last_three_words_of_an_answer\": \"development of applications\""
                                + "  }"
                                + "]}";

                // Text content that contains the answer
                String textContent = "Spring Boot is a framework that makes it easier for developers to create standalone, "
                                + "production-grade Spring-based Applications with minimal effort. It provides defaults for code "
                                + "and annotation configuration to simplify the development of applications.";

                // Create a real ChatResponse with our test content
                AssistantMessage assistantMessage = new AssistantMessage(jsonContent);
                Generation generation = new Generation(assistantMessage);
                ChatResponse chatResponse = new ChatResponse(Collections.singletonList(generation));

                // Execute the method with the real ChatResponse and text content
                List<QAModel> result = formatService.formatQAModels(chatResponse, textContent);

                // Verify results
                assertEquals(1, result.size());
                assertEquals("What is Spring Boot?", result.get(0).getQuestion());

                // The answer should be extracted from textContent between "Spring Boot is" and
                // "development of applications."
                String expectedAnswer = "Spring Boot is a framework that makes it easier for developers to create standalone, "
                                + "production-grade Spring-based Applications with minimal effort. It provides defaults for code "
                                + "and annotation configuration to simplify the development of applications";
                assertEquals(expectedAnswer, result.get(0).getAnswer());
        }

        @Test
        public void testFormatDataModelToInformationPiece() {
                // Setup test data
                String testQuery = "spring boot tutorial";
                String testTitle = "Introduction to Spring Boot";
                String testContent = "Spring Boot makes it easy to create stand-alone applications";
                String testDescription = "Learn about Spring Boot basics";
                String testLink = "https://example.com/spring-boot";
                String testAuthor = "John Doe";
                String testPublishDate = "2023-05-01";

                ScrapeItem item = ScrapeItem.builder().title(testTitle).content(testContent)
                                .description(testDescription).link(testLink).publishDate(testPublishDate)
                                .author(testAuthor).build();

                Exception testError = new RuntimeException("Test error");

                DataModel dataModel = DataModel.builder()
                                .query(testQuery)
                                .items(Arrays.asList(item))
                                .errors(Arrays.asList(testError))
                                .build();

                // Execute the method
                List<InformationPiece> result = formatService.format(dataModel);

                // Verify results
                assertThat(result).isNotNull();
                assertThat(result.size()).isEqualTo(1);

                InformationPiece piece = result.get(0);
                assertThat(piece.getQuery()).isEqualTo(testQuery);
                assertThat(piece.getTitle()).isEqualTo(testTitle);
                assertThat(piece.getContent()).isEqualTo(testContent);
                assertThat(piece.getDescription()).isEqualTo(testDescription);
                assertThat(piece.getLink()).isEqualTo(testLink);
                assertThat(piece.getAuthor()).isEqualTo(testAuthor);
                assertThat(piece.getPublishDate()).isEqualTo(testPublishDate);
                assertThat(piece.getError()).isEqualTo(dataModel.getErrors());
        }
}
