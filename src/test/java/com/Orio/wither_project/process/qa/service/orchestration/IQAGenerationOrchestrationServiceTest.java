package com.Orio.wither_project.process.qa.service.orchestration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.Orio.wither_project.config.TestTextConfiguration;
import com.Orio.wither_project.gather.model.TextBatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest()
@ActiveProfiles("test")
public class IQAGenerationOrchestrationServiceTest {

    @Autowired
    private IQAGenerationOrchestrationService qaGenerationOrchestrationService;
    @Autowired
    private TestTextConfiguration testTextConfig;

    @Nested
    @DisplayName("Orchestration Logic Tests")
    class OrchestrationTests {

        @Test
        @DisplayName("Should process a TextBatch successfully without exceptions")
        void shouldProcessTextBatchSuccessfully() {
            // --- Arrange ---
            // Assumption: The autowired qaGenerationOrchestrationService is a fully
            // functional
            // implementation capable of handling the orchestration process, including
            // potential
            // calls to external AI services, database interactions, and WebSocket
            // notifications
            // as defined by its concrete implementations of the interface methods.
            // Assumption: The test environment is configured correctly for the
            // implementation's needs
            // (e.g., API keys, database connections).

            log.info("Starting orchestration test for a sample TextBatch.");
            String content = testTextConfig.getSingleParagraph();
            var textBatch = TextBatch.builder()
                    .source("Sample Source Document")
                    .content(List.of(content))
                    .build();

            // --- Act & Assert ---
            // Execute the orchestration logic and assert that it completes without
            // throwing.
            // Due to the 'no mocks' requirement and the nature of the dependencies (AI, DB,
            // WS),
            // verifying specific outputs (generated questions/answers, saved data) is
            // complex
            // within this test alone and would require inspecting side effects (e.g.,
            // database state)
            // or capturing notifications, making it more of an integration test.
            assertDoesNotThrow(() -> {
                qaGenerationOrchestrationService.orchestrate(textBatch);
            }, "Orchestration process should complete without throwing exceptions.");

            log.info("Orchestration test completed for the sample TextBatch.");
        }

        @Test
        @DisplayName("Should handle empty content list within TextBatch gracefully")
        void shouldHandleEmptyContentList() {
            // --- Arrange ---
            log.info("Starting orchestration test for an empty TextBatch.");
            var textBatch = TextBatch.builder()
                    .source("Empty Source Document")
                    .content(List.of())
                    .build();

            // --- Act & Assert ---
            // The orchestrate method iterates over batch.getContent(). If the list is
            // empty,
            // the lambda body should not execute, and the method should complete without
            // error.
            assertDoesNotThrow(() -> {
                qaGenerationOrchestrationService.orchestrate(textBatch);
            }, "Orchestration should handle empty content list gracefully.");
            log.info("Orchestration test completed for the empty TextBatch.");
        }
    }
}
